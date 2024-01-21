package ru.sitronics.velobike.domain.rent

import kotlinx.coroutines.delay
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.MapContentRepository
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
    appContextProvider: AppContextProvider,
) : BaseUseCase(appContextProvider) {
    private var rentStatus: RentStatus? = null
    private var activeRentUpdateTask: TimerTask? = null
    private var activeRent: ActiveRent? = null

    fun startRent(
        bikeId: String, latitude: Double?, longitude: Double?,
        onError: (String?) -> Unit, onSuccess: (ActiveRent?, Boolean) -> Unit
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
                }
                Logg.d("!!!! startRent end, status $rentStatus")

                if (rentStatus?.status == MainRentStatus.IN_PROGRESS)
                    checkActiveRent(onError, onSuccess)
                else
                    onError(getRentError(it.failedReason, true))
            },
            onError = {
                Logg.d("!!!! ERROR startRent()")
                onError(context.getString(R.string.error_unknown))
            },
            callName = "startRent"
        )
    }

    private fun checkRentStatus(rentId: Int, deviceId: String) {
        Logg.d("!1 checkRentStatus start")
        processNetworkCall(
            action = { rentRepository.checkStatus(rentId, deviceId) },
            onSuccess = {
                Logg.d("!1 checkRentStatus success, status ${it.status} , ${it.processStatus}")
                rentStatus = it
            },
            onError = {
                Logg.d("!1 checkRentStatus ERROR")
                rentStatus = rentStatus?.copy(status = MainRentStatus.ERROR_START)
            },
            callName = "checkRentStatus"
        )
    }

    private fun getRentError(failedReason: FailedReason?, startRent: Boolean) : String {
        return failedReason?.let {
            context.getString( if (startRent) it.messageIdStart else it.messageIdFinish)
        } ?: context.getString(R.string.start_omni_failed_default)
    }

    fun finishRent(
        latitude: Double?, longitude: Double?,
        onError: (String?) -> Unit, onSuccess: suspend (ActiveRent?, Boolean) -> Unit
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

// TODO: consider the case when the lock is already closed !
                while (rentStatus?.processStatus != ProgressStatus.WAIT_CLOSE_LOCK) {
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

    fun updateActiveRent(
        isStart: Boolean,
        onError: (String?) -> Unit,
        onSuccess: suspend (ActiveRent?, Boolean) -> Unit,
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
        onError: (String?) -> Unit, onSuccess: suspend (ActiveRent?, Boolean) -> Unit
    ) {
        processNetworkCall(
            action = { rentRepository.checkActiveRent() },
            onSuccess = {
                Logg.d("!1 checkActiveRent found ${it.size}")

                val isChanged = activeRent?.rentStatus != it.firstOrNull()?.rentStatus
                Logg.d("!!!! checkActiveRent ${activeRent?.rentStatus?.name} ${it.firstOrNull()?.rentStatus?.name} $isChanged")
                activeRent = it.firstOrNull()
                runWithBike(activeRent?.frameNumber) { bike ->
                    activeRent?.bike = bike
                    onSuccess(activeRent, isChanged)
                }
/*
                // update rent only if !isActiveRentClosed and state changed
                if (it.isNotEmpty() && !isActiveRentClosed) {
                    activeRent = it[0]
                    runWithBike(activeRent!!.frameNumber) { bike ->
                        activeRent!!.bike = bike
                        onSuccess(activeRent)
                    }
                } else if (activeRent != null && !isActiveRentClosed) {
                    activeRent = null
                    onSuccess(null)
                }
*/
            },
            onError = {
                Logg.d("!1 ERROR checkActiveRent()")
                onError(null)
            },
            callName = "checkActiveRent"
        )
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

    companion object {
        private const val CHECK_RENT_STATUS_DELAY = 3000L
        private const val CHECK_ACTIVE_RENT_DELAY = 10000L
    }
}