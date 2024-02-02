package ru.sitronics.velobike.domain.rent

import kotlinx.coroutines.delay
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.MapContentRepository
import ru.sitronics.velobike.domain.profile.ProfileData
import ru.sitronics.velobike.domain.profile.ProfileRepository
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
    private val profileRepository: ProfileRepository,
    appContextProvider: AppContextProvider,
) : BaseUseCase(appContextProvider) {
    var activeRent: ActiveRent? = null
        private set
    private var rentStatus: RentStatus? = null
    private var activeRentUpdateTask: TimerTask? = null
    private var userId: String? = null

    fun onMapStart() {
        getProfile()
    }

    fun startRent(
        bikeId: String, latitude: Double?, longitude: Double?,
        onError: (String?) -> Unit, onSuccess: (ActiveRent?) -> Unit
    ) {
        val params = StartRentParams(
            bikeSerialNumber = bikeId,
            isUsedQr = true,
            clientGeoPosition = ClientGeoPosition(
                lat = latitude ?: 0.0,
                lon = longitude ?: 0.0,
            ),
        )

        processNetworkCall(
            action = { rentRepository.startRent(params) },
            onSuccess = {
                Logg.d("!!!! startRent success, status ${it.status}")
                rentStatus = it
                delay(CHECK_RENT_STATUS_DELAY)

                while (rentStatus?.status == MainRentStatus.CHECK_START) {
                    checkRentStatus(it.id, it.deviceId ?: "")
                    delay(CHECK_RENT_STATUS_DELAY)
                    Logg.d("!!!! startRent while, status ${rentStatus?.status} ${rentStatus?.processStatus}")
                }
                Logg.d("!!!! startRent end, status ${rentStatus?.status} ${rentStatus?.processStatus}")

                if (rentStatus?.status == MainRentStatus.IN_PROGRESS)
                    checkActiveRent(onError, onSuccess)
                else
                    onError(getRentError(it.failedReason, true))
            },
            onError = {
                Logg.d("!!!! ERROR startRent() ${it.message}")
                onError(context.getString(R.string.error_unknown))
            },
            callName = "startRent"
        )
    }

    fun finishRent(
        latitude: Double?, longitude: Double?,
        onError: (String?) -> Unit, onSuccess: suspend (ActiveRent?) -> Unit
    ) {
        if (activeRent == null) return

        val params = FinishRentParams(
            id = activeRent?.rentId ?: 0,
            deviceId = activeRent?.frameNumber ?: "",
            bikeSerialNumber = activeRent?.bike?.bikeSerialNumber ?: "",
            clientGeoPosition = ClientGeoPosition(
                lat = latitude ?: 0.0,
                lon = longitude ?: 0.0,
            ),
        )

        Logg.d("!1 finishRent start")
        processNetworkCall(
            action = { rentRepository.finishRent(params) },
            onSuccess = {
                Logg.d("!1 finishRent success, status ${it.status}, ${it.processStatus}")
                rentStatus = it
                delay(CHECK_RENT_STATUS_DELAY)

                while (rentStatus?.processStatus == ProgressStatus.S5_OBTAIN_LOCK_INFO) {
                    checkRentStatus(it.id, it.deviceId ?: "")
                    delay(CHECK_RENT_STATUS_DELAY)
                }

                checkActiveRent(onError, onSuccess)
            },
            onError = {
                Logg.d("!1 finishRent ERROR")
                onError(context.getString(R.string.error_unknown))
            },
            callName = "finishRent"
        )
    }

    fun checkRentStatus(
        rentId: Int, deviceId: String,
        onError: ((String?) -> Unit)? = null, onSuccess: (suspend (RentStatus) -> Unit)? = null
        ) {
        Logg.d("!!!! checkRentStatus start")
        processNetworkCall(
            action = { rentRepository.checkStatus(rentId, deviceId) },
            onSuccess = {
                Logg.d("!!!! checkRentStatus success, status ${it.status}, ${it.processStatus}")
                rentStatus = it
                onSuccess?.invoke(it)
            },
            onError = {
                Logg.d("!!!! checkRentStatus ERROR")
                rentStatus = rentStatus?.copy(status = MainRentStatus.ERROR_START)
                onError?.invoke(null)
            },
            callName = "checkRentStatus"
        )
    }

    fun chooseParking(
        rentId: Int, params: ChooseParkingParams,
        onError: (String?) -> Unit, onSuccess: suspend () -> Unit
    ) {
        Logg.d("!!!! chooseParking start")
        processNetworkCall(
            action = { rentRepository.chooseParking(rentId, params) },
            onSuccess = {
                Logg.d("!!!! chooseParking success, status ${it.status}, ${it.processStatus}")
                rentStatus = it
                delay(CHECK_RENT_STATUS_DELAY)

                while (rentStatus?.processStatus == ProgressStatus.S5_OBTAIN_LOCK_INFO) {
                    checkRentStatus(it.id, it.deviceId ?: "")
                    delay(CHECK_RENT_STATUS_DELAY)
                }

                onSuccess()
            },
            onError = {
                Logg.d("!!!! chooseParking ERROR")
                onError(null)
            },
            callName = "chooseParking"
        )
    }

    fun uploadPhotoAndFinishRent(
        filePath: String,
        onError: (String?) -> Unit, onSuccess: suspend (RentStatus) -> Unit
    ) {
        uploadPhotoRent(filePath, onError) {
            finishRentAfterUploadPhoto(onError, onSuccess)
        }
    }

    fun uploadPhotoRent(
        filePath: String,
        onError: (String?) -> Unit, onSuccess: suspend () -> Unit
    ) {
        Logg.d("!!!! uploadPhotoRent start")
        processNetworkCall(
            action = {
                rentRepository.uploadPhotoRent(
                    activeRent?.rentId ?: 0,
                    activeRent?.frameNumber ?: "",
                    filePath
                )
            },
            onSuccess = {
                Logg.d("!!!! uploadPhotoRent success $it")
                onSuccess()
            },
            onError = {
                Logg.d("!!!! uploadPhotoRent ERROR")
                onError(context.getString(R.string.error_unknown))
            },
            callName = "uploadPhotoRent"
        )
    }

    fun finishRentAfterUploadPhoto(
        onError: (String?) -> Unit, onSuccess: suspend (RentStatus) -> Unit
    ) {
        Logg.d("!!!! finishRentAfterUploadPhoto start")
        processNetworkCall(
            action = { rentRepository.finishRentAfterUploadPhoto(activeRent?.rentId ?: 0) },
            onSuccess = {
                Logg.d("!!!! finishRentAfterUploadPhoto success $it")
                onSuccess(it)
            },
            onError = {
                Logg.d("!!!! finishRentAfterUploadPhoto ERROR")
                onError(context.getString(R.string.error_unknown))
            },
            callName = "finishRentAfterUploadPhoto"
        )
    }

    private fun getRentError(failedReason: FailedReason?, startRent: Boolean) : String {
        return failedReason?.let {
            context.getString( if (startRent) it.messageIdStart else it.messageIdFinish)
        } ?: context.getString(R.string.start_omni_failed_default)
    }

    fun updateActiveRent(
        isStart: Boolean,
        onError: (String?) -> Unit,
        onSuccess: suspend (ActiveRent?) -> Unit,
    ) {
        if (isStart && activeRentUpdateTask == null) {
            activeRentUpdateTask = Timer().schedule(0, CHECK_ACTIVE_RENT_DELAY) {
                checkActiveRent(onError, onSuccess)
            }
        } else if (!isStart) {
            activeRentUpdateTask?.cancel()
            activeRentUpdateTask = null
        }
    }

    private fun checkActiveRent(
        onError: (String?) -> Unit, onSuccess: suspend (ActiveRent?) -> Unit
    ) {
        processNetworkCall(
            action = { rentRepository.checkActiveRent() },
            onSuccess = {
                Logg.d("!!!! checkActiveRent found ${it.size}")

                it.firstOrNull()?.let { rent ->
                    onCheckActiveRentSuccess(rent, onSuccess)
                } ?: checkActiveRentOld(onError, onSuccess)
            },
            onError = {
                Logg.d("!!!! ERROR checkActiveRent()")
                onError(null)
            },
            callName = "checkActiveRent"
        )
    }

    private fun checkActiveRentOld(
        onError: (String?) -> Unit, onSuccess: suspend (ActiveRent?) -> Unit
    ) {
        userId?.let {
            processNetworkCall(
                action = { rentRepository.checkActiveRentOld(it) }, //"4002600"
                onSuccess = {
                    Logg.d("!!!! checkActiveRentOld found ${it.size}")
                    onCheckActiveRentSuccess(it.firstOrNull(), onSuccess)
                },
                onError = {
                    Logg.d("!!!! ERROR checkActiveRentOld()")
                    onError(null)
                },
                callName = "checkActiveRentOld"
            )
        } ?: {
            Logg.d("!!!! no user error")
            onError(null)
        }
    }

    private suspend fun onCheckActiveRentSuccess(rent: ActiveRent?, onSuccess: suspend (ActiveRent?) -> Unit) {
        Logg.d("!!!! checkActiveRent ${activeRent?.rentStatus?.name} ${rent?.rentStatus?.name}")
        activeRent = rent
        runWithBike(activeRent?.frameNumber) { bike ->
            activeRent?.bike = bike
            onSuccess(activeRent)
        }
    }

    private suspend fun runWithBike(id: String?, action: suspend (Bike?) -> Unit) {
        if (id == null) {
            action(null)
            return
        }
        var bike = rentRepository.getData().activeRentBike
        if (bike != null) {
            action(bike)
        } else {
            bike = mapContentRepository.getData().bikes?.find { it.id == id }
            if (bike != null) {
                rentRepository.saveData(rentRepository.getData().copy(activeRentBike = bike))
                action(bike)
            } else {
                Logg.d("!1 runWithBike processNetworkCall $id")
                processNetworkCall(
                    action = { mapContentRepository.getBike(id) },
                    onSuccess = {
                        Logg.d("!1 runWithBike success ${it.id}")
                        rentRepository.saveData(rentRepository.getData().copy(activeRentBike = it))
                        action(it)
                    },
                    onError = { Logg.d("!1 runWithBike ERROR") },
                )
            }
        }
    }

    private fun getProfile() {
        userId = profileRepository.getData().profile?.userId
        if (userId == null) {
            processNetworkCall(
                action = { profileRepository.getProfile() },
                onSuccess = {
                    val profileData = ProfileData(it)
                    profileRepository.saveData(profileData)
                    userId = it.userId
                    Logg.d("!!!! getProfile success")
                },
                onError = { },
                callName = "getProfile"
            )
        }
    }

    companion object {
        private const val CHECK_RENT_STATUS_DELAY = 3000L
        private const val CHECK_ACTIVE_RENT_DELAY = 10000L
    }
}