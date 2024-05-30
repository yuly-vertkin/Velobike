package ru.sitronics.velobike.domain.rent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.MapContentRepository
import ru.sitronics.velobike.domain.profile.ProfileUseCase
import ru.sitronics.velobike.presentation.BaseUseCase
import ru.sitronics.velobike.tools.Logg
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.schedule

@Singleton
class RentUseCase @Inject constructor(
    private val rentRepository: RentRepository,
    private val mapContentRepository: MapContentRepository,
    private val profileUseCase: ProfileUseCase,
    private val authManager: AuthManager,
    appContextProvider: AppContextProvider,
) : BaseUseCase(appContextProvider) {
    var rent: Rent? = null
        private set
    private var finishedRent: Rent? = null
    private var rentStatus: RentStatus? = null
    private var activeRentUpdateTask: TimerTask? = null

    override fun initScope(vmScope: CoroutineScope) {
        super.initScope(vmScope)
        profileUseCase.initScope(vmScope)
    }

    override fun clearScope() {
        super.clearScope()
        profileUseCase.clearScope()
    }

    fun startRent(
        bikeId: String, latitude: Double?, longitude: Double?,
        onError: (String?) -> Unit, onSuccess: (Rent?) -> Unit
    ) {
        val params = StartRentParams(
            frameNumber = bikeId,
            isUsedQr = true,
            clientGeoPosition = ClientGeoPosition(
                lat = latitude ?: 0.0,
                lon = longitude ?: 0.0,
            ),
        )

        processNetworkCall(
            action = { rentRepository.startRent(params) },
            onSuccess = {
                Logg.d("!!! startRent status ${it.status}")
                rentStatus = it
//                delay(CHECK_RENT_STATUS_DELAY)

                while (rentStatus?.status == MainRentStatus.CHECK_START) {
                    checkRentStatus(it.id, it.frameNumber ?: "")
                    delay(CHECK_RENT_STATUS_DELAY)
                }

                if (rentStatus?.status == MainRentStatus.IN_PROGRESS)
                    checkActiveRent(onError, onSuccess)
                else
                    onError(getRentError(it.failedReason, true))
            },
            onError = {
                onError(context.getString(R.string.error_unknown))
            },
            callName = "startRent"
        )
    }

    fun finishRent(
        latitude: Double?, longitude: Double?,
        onError: (String?) -> Unit, onSuccess: (Rent?) -> Unit
    ) {
        if (rent == null) return

        val params = FinishRentParams(
            id = rent?.rentId.orEmpty(),
            frameNumber = rent?.frameNumber ?: "",
            deviceId = rent?.bike?.deviceId ?: "",
            clientGeoPosition = ClientGeoPosition(
                lat = latitude ?: 0.0,
                lon = longitude ?: 0.0,
            ),
        )

        processNetworkCall(
            action = { rentRepository.finishRent(params) },
            onSuccess = {
                Logg.d("!!! finishRent status ${it.status}, ${it.processStatus}")
                rentStatus = it
//                delay(CHECK_RENT_STATUS_DELAY)

                while (rentStatus?.processStatus == ProgressStatus.S5_OBTAIN_LOCK_INFO ||
                       rentStatus?.processStatus == ProgressStatus.S6_OBTAIN_SINGLE_RIDING) {
                    checkRentStatus(it.id, it.frameNumber ?: "")
                    delay(CHECK_RENT_STATUS_DELAY)
                }

                checkActiveRent(onError, onSuccess)
            },
            onError = {
                onError(context.getString(R.string.error_unknown))
            },
            callName = "finishRent"
        )
    }

    fun checkRentStatus(
        rentId: String, frameNumber: String,
        onError: ((String?) -> Unit)? = null, onSuccess: ((RentStatus) -> Unit)? = null
    ) {
        processNetworkCall(
            action = { rentRepository.checkStatus(rentId, frameNumber) },
            onSuccess = {
                Logg.d("!!! checkRentStatus status ${it.status}, ${it.processStatus}")
                rentStatus = it
                onSuccess?.invoke(it)
            },
            onError = {
                rentStatus = rentStatus?.copy(status = MainRentStatus.ERROR_START)
                onError?.invoke(null)
            },
            callName = "checkRentStatus"
        )
    }

    fun chooseParking(
        rentId: String, params: ChooseParkingParams,
        onError: (String?) -> Unit, onSuccess: () -> Unit
    ) {
        processNetworkCall(
            action = { rentRepository.chooseParking(rentId, params) },
            onSuccess = {
                Logg.d("!!! chooseParking status ${it.status}, ${it.processStatus}")
                rentStatus = it
                delay(CHECK_RENT_STATUS_DELAY)

                while (rentStatus?.processStatus == ProgressStatus.S5_OBTAIN_LOCK_INFO) {
                    checkRentStatus(it.id, it.frameNumber ?: "")
                    delay(CHECK_RENT_STATUS_DELAY)
                }

                onSuccess()
            },
            onError = { onError(null) },
            callName = "chooseParking"
        )
    }

    fun uploadPhotoAndFinishRent(
        filePath: String,
        onError: (String?) -> Unit, onSuccess: (RentStatus) -> Unit
    ) {
        uploadPhotoRent(filePath, onError) {
            finishRentAfterUploadPhoto(onError, onSuccess)
        }
    }

    fun uploadPhotoRent(
        filePath: String,
        onError: (String?) -> Unit, onSuccess: () -> Unit
    ) {
        processNetworkCall(
            action = { rentRepository.uploadPhotoRent(rent?.rentId.orEmpty(), filePath) },
            onSuccess = { onSuccess() },
            onError = { onError(context.getString(R.string.error_unknown)) },
            callName = "uploadPhotoRent"
        )
    }

    fun finishRentAfterUploadPhoto(
        onError: (String?) -> Unit, onSuccess: (RentStatus) -> Unit
    ) {
        processNetworkCall(
            action = { rentRepository.finishRentAfterUploadPhoto(rent?.rentId.orEmpty()) },
            onSuccess = {
                Logg.d("!!! finishRentAfterUploadPhoto status ${it.status}, ${it.processStatus}")
                onSuccess(it)
            },
            onError = {
                onError(context.getString(R.string.error_unknown))
            },
            callName = "finishRentAfterUploadPhoto"
        )
    }

    fun sendFeedback(
        rent: Rent?, rate: Int,
        onError: (String?) -> Unit, onSuccess: () -> Unit
    ) {
        rent?.let {
            val params = Feedback(
                comments = "",
                vehicleFrameNumber = it.frameNumber,
                rentId = it.rentId,
                rate = rate.toString(),
                customerExternalId = authManager.userId.orEmpty(),
                vehicleType = if (it.rentId.length > 6) "ELECTRICAL" else "OMNI",
                // temp if rate < 5 it's needed to choose reason
                handlebar = rate < 5
            )

            processNetworkCall(
                action = { rentRepository.sendFeedback(params) },
                onSuccess = { onSuccess() },
                onError = { onError(context.getString(R.string.error_unknown)) },
                callName = "sendFeedback"
            )
        }
    }

    fun returnToActiveRent(
        rentId: String, onError: (String?) -> Unit, onSuccess: (Rent?) -> Unit
    ) {
        processNetworkCall(
            action = { rentRepository.returnToActiveRent(rentId) },
            onSuccess = {
                Logg.d("!!! returnToActiveRent status ${it.status}, ${it.processStatus}")
                if (it.failedReason == null)
                    checkActiveRent(onError, onSuccess)
                else
                    onError(getRentError(it.failedReason, false))
            },
            onError = { onError(context.getString(R.string.error_unknown)) },
            callName = "returnToActiveRent"
        )
    }

    fun unlockWheel(onError: (String?) -> Unit, onSuccess: () -> Unit) {
        processNetworkCall(
            action = {
                rentRepository.unlockWheel(rent?.rentId.orEmpty())
            },
            onSuccess = { onSuccess() },
            onError = { onError(context.getString(R.string.error_unknown)) },
            callName = "unlockWheel"
        )
    }

    private fun getRentError(failedReason: FailedReason?, startRent: Boolean) : String {
        return failedReason?.let {
            context.getString( if (startRent) it.messageIdStart else it.messageIdFinish)
        } ?: context.getString(R.string.start_omni_failed_default)
    }

    fun updateActiveRent(
        needStop: Boolean,
        onError: (String?) -> Unit, onSuccess: (Rent?) -> Unit, onFinish: ((Rent?) -> Unit)? = null
    ) {
        var needStart = !needStop

        if (activeRentUpdateTask == null) {
            activeRentUpdateTask = Timer().schedule(0, CHECK_ACTIVE_RENT_DELAY) {
                if (needStart || rent != null) {
                    checkActiveRent(onError, onSuccess, onFinish)
                    needStart = false
                }
            }
        }

        if (needStop) {
            activeRentUpdateTask?.cancel()
            activeRentUpdateTask = null
        }
    }

    private fun checkActiveRent(
        onError: (String?) -> Unit, onSuccess: (Rent?) -> Unit, onFinish: ((Rent?) -> Unit)? = null
    ) {
        processNetworkCall(
            action = { rentRepository.checkActiveRent() },
            onSuccess = { rents ->
                Logg.d("!!! checkActiveRent found ${rents.size}")
                val curRent = rents.firstOrNull()

                // check if rent (not old) has just finished
                if (curRent == null && rent?.isOld == false) {
                    val finRent = rent
                    onCheckActiveRentSuccess(null, onSuccess)
                    onFinish?.invoke(finRent)
                    return@processNetworkCall
                }

                curRent?.let {
                    onCheckActiveRentSuccess(it, onSuccess)
                } ?: checkActiveRentOld(onError, onSuccess, onFinish)
            },
            onError = { onError(null) },
            callName = "checkActiveRent"
        )
    }

    private fun checkActiveRentOld(
        onError: (String?) -> Unit, onSuccess: (Rent?) -> Unit, onFinish: ((Rent?) -> Unit)? = null
    ) {
        authManager.userId?.let {
            processNetworkCall(
                action = { rentRepository.checkActiveRentOld(it) },
                onSuccess = {
                    Logg.d("!!! checkActiveRentOld found ${it.size}")
                    val curRent = it.firstOrNull()
                    curRent?.isOld = true

                    // check if old rent has just finished
                    if (curRent == null && rent?.isOld == true)
                        finishedRent = rent

                    onCheckActiveRentSuccess(curRent, onSuccess)

                    finishedRent?.let {
                        checkFinishedRentOld(onFinish)
                    }
                },
                onError = { onError(null) },
                callName = "checkActiveRentOld"
            )
        } ?: {
            Logg.d("!!! no user error")
            scope?.launch { onError(null) }
        }
    }

    private fun checkFinishedRentOld(
        onFinish: ((Rent?) -> Unit)?
    ) {
        authManager.userId?.let { userId ->
            finishedRent?.let {
                processNetworkCall(
                    action = { rentRepository.checkFinishedRentOld(userId, it.rentId) },
                    onSuccess = { rents ->
                        Logg.d("!!! checkFinishedRentOld found ${rents.size}")
                        rents.firstOrNull()?.let {
                            val rent = finishedRent?.copy(rentId = it.rentId)
                            onFinish?.invoke(rent)
                            finishedRent = null
                        }
                    },
                    onError = {},
                    callName = "checkFinishedRentOld"
                )
            }
        }
    }

    private fun onCheckActiveRentSuccess(
        rent: Rent?, onSuccess: (Rent?) -> Unit
    ) {
        Logg.d("!!! checkActiveRent ${this.rent?.rentStatus?.name} ${rent?.rentStatus?.name}")

        rent?.let {
            // save old cost before calculate new one
            it.cost = this.rent?.cost ?: 0
            profileUseCase.calculateRentCost(it.startTime, it.isOld) { cost ->
                it.cost = cost
            }
            it.showFine = profileUseCase.isFine(it.startTime)
        }

        if (rent != null && !rent.isOld)
            runWithBike(rent.frameNumber) { bike ->
                rent.bike = bike
                this.rent = rent
                onSuccess(rent)
            }
        else {
            this.rent = rent
            onSuccess(rent)
        }
    }

    private fun runWithBike(id: String, action: (Bike?) -> Unit) {
        var bike = rentRepository.getData().rentBike
        if (bike != null) {
            action(bike)
        } else {
            bike = mapContentRepository.getData().bikes?.find { it.id == id }
            if (bike != null) {
                rentRepository.saveData(rentRepository.getData().copy(rentBike = bike))
                action(bike)
            } else {
                processNetworkCall(
                    action = { mapContentRepository.getBike(id) },
                    onSuccess = {
                        Logg.d("!!! runWithBike id ${it.id}")
                        rentRepository.saveData(rentRepository.getData().copy(rentBike = it))
                        action(it)
                    },
                    onError = {},
                )
            }
        }
    }

    companion object {
        private const val CHECK_RENT_STATUS_DELAY = 3000L
        private const val CHECK_ACTIVE_RENT_DELAY = 10000L
    }
}