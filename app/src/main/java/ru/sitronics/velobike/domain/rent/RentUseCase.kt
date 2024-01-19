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
    private var isActiveRent: Boolean = false
    var isActiveRentClosed: Boolean = false

    fun startRent(
        bikeId: String, latitude: Double?, longitude: Double?,
        onSuccess: (ActiveRent?) -> Unit, onError: (String?) -> Unit
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
                    checkActiveRent(onSuccess, onError)
                else
                    onError(getRentError(it.failedReason, true))
            },
            onError = {
                Logg.d("!!!! ERROR startRent()")
                onError(context.getString(R.string.error_unknown))
            },
        )
    }

    private fun checkRentStatus(rentId: Int, deviceId: String) {
        processNetworkCall(
            action = { rentRepository.checkStatus(rentId, deviceId) },
            onSuccess = {
                Logg.d("!!!! checkRentStatus success, status ${it.status}")
                rentStatus = it
            },
            onError = {
                Logg.d("!!! ERROR checkRentStatus()")
                rentStatus = rentStatus?.copy(status = MainRentStatus.ERROR_START)
            },
        )
    }

    private fun getRentError(failedReason: FailedReason?, startRent: Boolean) : String {
        return failedReason?.let {
            context.getString( if (startRent) it.messageIdStart else it.messageIdFinish)
        } ?: context.getString(R.string.start_omni_failed_default)
    }

    fun finishRent(
        latitude: Double?, longitude: Double?,
        onSuccess: () -> Unit, onError: (String?) -> Unit
    ) {
        if (rentStatus == null) return

        val params = FinishRentParams(
            id = rentStatus!!.id,
            deviceId = rentStatus!!.deviceId ?: "",
            bikeSerialNumber = rentStatus!!.bikeSerialNumber ?: "",
            clientGeoPosition = ClientGeoPosition(
                lat = latitude ?: 0.0,
                lon = longitude ?: 0.0,
            ),
        )

        processNetworkCall(
            action = { rentRepository.finishRent(params) },
            onSuccess = {
                Logg.d("!!!! finishRent success, status ${it.status}")
                rentStatus = it
                delay(CHECK_RENT_STATUS_DELAY)

                while (rentStatus?.status == MainRentStatus.CHECK_END) {
                    checkRentStatus(it.id, it.deviceId ?: "")
                    delay(CHECK_RENT_STATUS_DELAY)
                }

                Logg.d("!!!! finishRent end, status $rentStatus")
// TODO: continue finish rent
                if (rentStatus?.status == MainRentStatus.IN_PROGRESS)
                    onSuccess()
                else
                    onError(getRentError(it.failedReason, true))
            },
            onError = {
                Logg.d("!!!! ERROR finishRent()")
                onError(context.getString(R.string.error_unknown))
            },
        )
    }

    fun updateActiveRent(
        isStart: Boolean,
        onSuccess: suspend (ActiveRent?) -> Unit, onError: (String?) -> Unit
    ) {
        if (isStart && activeRentUpdateTask == null) {
            activeRentUpdateTask = Timer().schedule(0, CHECK_ACTIVE_RENT_DELAY) {
                checkActiveRent(onSuccess, onError)
            }
        } else if (!isStart) {
            activeRentUpdateTask?.cancel()
            activeRentUpdateTask = null
        }
    }

    private fun checkActiveRent(
        onSuccess: suspend (ActiveRent?) -> Unit, onError: (String?) -> Unit
    ) {
        processNetworkCall(
            action = { rentRepository.checkActiveRent() },
            onSuccess = {
                Logg.d("!!! checkActiveRent found ${it.size}")
                // update rent only if !isActiveRentClosed and state changed
                if (it.isNotEmpty() && !isActiveRentClosed) {
                    val activeRent = it[0]
                    runWithBike(activeRent.frameNumber) { bike ->
                        activeRent.bike = bike
                        onSuccess(activeRent)
                    }
                } else if (isActiveRent && !isActiveRentClosed) {
                    onSuccess(null)
                }
                isActiveRent = it.isNotEmpty()
            },
            onError = {
                Logg.d("!!! ERROR checkActiveRent()")
                onError(null)
            },
            callName = "checkActiveRent"
        )
    }

    private suspend fun runWithBike(id: String, action: suspend (Bike) -> Unit) {
        var bike = rentRepository.getData().activeRentBike
        if (bike != null) {
            action(bike)
        } else {
            bike = mapContentRepository.getData().bikes?.find { it.id == id }
            if (bike != null) {
                rentRepository.saveData(rentRepository.getData().copy(activeRentBike = bike))
                action(bike)
            } else {
                Logg.d("!!!! runWithBike processNetworkCall $id")
                processNetworkCall(
                    action = { mapContentRepository.getBike(id) },
                    onSuccess = {
                        Logg.d("!!!! getBike ${it.id}")
                        rentRepository.saveData(rentRepository.getData().copy(activeRentBike = it))
                        action(it)
                    },
                    onError = { Logg.d("!!!! ERROR getBike") },
                )
            }
        }
    }


    companion object {
        private const val CHECK_RENT_STATUS_DELAY = 3000L
        private const val CHECK_ACTIVE_RENT_DELAY = 10000L
    }
}