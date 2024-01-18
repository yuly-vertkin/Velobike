package ru.sitronics.velobike.presentation.map

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.content.Bike
import ru.sitronics.velobike.domain.content.MapContentRepository
import ru.sitronics.velobike.domain.content.Parking
import ru.sitronics.velobike.domain.rent.ClientGeoPosition
import ru.sitronics.velobike.domain.rent.FailedReason
import ru.sitronics.velobike.domain.rent.FinishRentParams
import ru.sitronics.velobike.domain.rent.MainRentStatus
import ru.sitronics.velobike.domain.rent.RentRepository
import ru.sitronics.velobike.domain.rent.RentStatus
import ru.sitronics.velobike.domain.rent.StartRentParams
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import ru.sitronics.velobike.tools.filterMoveZones
import ru.sitronics.velobike.tools.filterSlowZones
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapContentRepository: MapContentRepository,
    private val rentRepository: RentRepository,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _mapUiState: MutableStateFlow<MapUiState> = MutableStateFlow(MapUiState.Normal)
    val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()
    private var prevMapUiState: MapUiState = MapUiState.Normal
    private var rentStatus: RentStatus? = null
    private var showSlowZones: Boolean = false
    private var showSlowZoneMarkers: Boolean = false
    private var activeRentUpdateTask: TimerTask? = null
    private var isActiveRent: Boolean = false

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.MapStart -> {
                updateMoveZones()
                updateActiveRent(true)
            }
            is MapIntent.MapStop -> {
                updateActiveRent(false)
            }
            is MapIntent.ChangeMapPosition -> {
                updateBikesAndParkings(intent.mapRect, intent.zoom)
                updateSlowZones(intent.zoom)
            }
            is MapIntent.MapObjectTap -> {
                when (intent.userData) {
                    is MarkerUserData.Bike -> onBikeClick(intent.userData.id)
                    is MarkerUserData.Station -> onStationClick(intent.userData.id)
                    is MarkerUserData.Parking -> onParkingClick(intent.userData.id)
                    is MarkerUserData.SlowZone -> {}//onSlowZoneClick(data)
                    is MarkerUserData.MoveZone -> {}//onNotMoveZoneClick()
                    null -> {}
                }
            }
            is MapIntent.CloseParkingDetail -> {
                changeState(MapUiState.Normal)
            }
            is MapIntent.CloseBikeDetail -> {
                if (prevMapUiState is MapUiState.ShowQrScan && intent.id != null) {
                    startRent(intent.id, intent.latitude, intent.longitude)
                } else {
                    changeState(if (intent.id != null) MapUiState.ShowQrScan else MapUiState.Normal)
                }
            }
            is MapIntent.QrScanTap -> {
                changeState(MapUiState.ShowQrScan)
            }
            is MapIntent.CloseQrScan -> {
                if (prevMapUiState is MapUiState.ShowBikeDetail && intent.id != null) {
                    startRent(intent.id, intent.latitude, intent.longitude)
                } else if (intent.id != null) {
                    runWithBike(intent.id) { bike ->
                        changeState(MapUiState.ShowBikeDetail(bike))
                    }
                } else {
                    changeState(MapUiState.Normal)
                }
            }
            is MapIntent.CloseError -> {
                changeState(MapUiState.Normal)
            }
            is MapIntent.CloseActiveRent -> {
                if (intent.close)
                    finishRent(intent.latitude, intent.longitude)
                else
                    changeState(MapUiState.Normal)

            }
        }
    }

    private fun changeState(uiState: MapUiState) {
        prevMapUiState = _mapUiState.value
        _mapUiState.value = uiState
    }

    private fun startRent(bikeId: String, latitude: Double?, longitude: Double?) {
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
                    checkActiveRent()
                else
                    changeState(MapUiState.ShowError(getRentError(it.failedReason, true)))
            },
            onError = {
                Logg.d("!!!! ERROR startRent()")
                changeState(MapUiState.ShowError(getRentError(null, true)))
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

    private fun finishRent(latitude: Double?, longitude: Double?) {
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
                changeState(
                    // TODO: continue finish rent
                    if (rentStatus?.status == MainRentStatus.IN_PROGRESS) MapUiState.Normal
                    else MapUiState.ShowError(getRentError(it.failedReason, true))
                )
            },
            onError = {
                Logg.d("!!!! ERROR finishRent()")
                changeState(MapUiState.ShowError(getRentError(null, false)))
            },
        )
    }

    private fun onBikeClick(id: String) {
        Logg.d("!!! onBikeClick $id")
        getBike(id)?.let { bike ->
            changeState(MapUiState.ShowBikeDetail(bike))
        }
    }

    private fun getBike(id: String) : Bike? =
        mapContentRepository.getData().bikes?.find { it.id == id }

    private fun runWithBike(id: String, action: (Bike) -> Unit) {
        Logg.d("!!!! runWithBike: $id")
        val bike = getBike(id)
        if (bike != null) {
            Logg.d("!!!! found Bike: $id")
            action(bike)
        } else {
            Logg.d("!!!! runWithBike processNetworkCall $id")
            processNetworkCall(
                action = { mapContentRepository.getBike(id) },
                onSuccess = {
                    Logg.d("!!!! getBike ${it.id}")
                    action(it)
                },
                onError = { Logg.d("!!!! ERROR getBike") },
            )
        }
    }

    private fun onStationClick(id: String) {
        Logg.d("!!! onStationClick $id")
        getStation(id)?.let { station ->
            changeState(MapUiState.ShowStationDetail(station))
        }
    }

    private fun getStation(id: String) : Parking? =
        mapContentRepository.getData().stations?.find { it.id == id }

    private fun onParkingClick(id: String) {
        Logg.d("!!! onParkingClick $id")
        getParking(id)?.let { parking ->
            changeState(MapUiState.ShowParkingDetail(parking))
        }
    }

    private fun getParking(id: String) : Parking? =
        mapContentRepository.getData().parkings?.find { it.id == id }

    private fun updateBikesAndParkings(mapRect: MapRect, zoom: Float) {
        if (zoom < SHOW_CONTENT_ZOOM) return

        processNetworkCall(
            action = { mapContentRepository.getBikes(mapRect) },
            onSuccess = { bikes ->
                Logg.d("!!! getBikes() ${bikes.size}")
                mapContentRepository.saveData(mapContentRepository.getData().copy(
                    bikes = bikes
                ))
                changeState(MapUiState.BikesUpdated(bikes))
            },
            onError = { Logg.d("!!! ERROR getBikes()") },
            force = true,
            callName = "getBikes"
        )

        processNetworkCall(
            action = { mapContentRepository.getParkings(mapRect) },
            onSuccess = { items ->
                val stations = items.filter { it.type.isStation() }
                val parkings = items.filter { it.type.isParking() }
                Logg.d("!!! getParkings() stations: ${stations.size}, parkings: ${parkings.size}")
                mapContentRepository.saveData(mapContentRepository.getData().copy(
                    stations = stations,
                    parkings = parkings,
                ))
                changeState(MapUiState.ParkingsUpdated(
                    stations,
                    if (zoom >= SHOW_PARKINGS_ZOOM) parkings else emptyList()
                ))
            },
            onError = { Logg.d("!!! ERROR getParkings() ${it.message}") },
            force = true,
            callName = "getParkings"
        )
    }

    private fun updateSlowZones(zoom: Float) {
        val show = zoom >= SHOW_SLOW_ZONE_ZOOM
        val showMarkers = zoom >= SHOW_SLOW_ZONE_MARKER_ZOOM
        if (show && mapContentRepository.getData().slowZones == null) {
            processNetworkCall(
                action = { mapContentRepository.getSlowZones() },
                onSuccess = {
                    Logg.d("!!! getSlowZones() ${it.size}")
                    mapContentRepository.saveData(
                        mapContentRepository.getData().copy(
                            slowZones = it
                        )
                    )
                    changeState(MapUiState.ShowSlowZones(filterSlowZones(it), showMarkers))
                },
                onError = { Logg.d("!!! ERROR getSlowZones()") },
                callName = "getSlowZones"
            )
        } else if (show != showSlowZones || showMarkers != showSlowZoneMarkers) {
            mapContentRepository.getData().slowZones?.let {
                changeState(MapUiState.ShowSlowZones(if (show) filterSlowZones(it) else emptyList(), showMarkers))
            }
        }
        showSlowZones = show
        showSlowZoneMarkers = showMarkers
    }

    private fun updateMoveZones() {
        if (mapContentRepository.getData().moveZones == null) {
            processNetworkCall(
                action = { mapContentRepository.getMoveZones() },
                onSuccess = {
                    Logg.d("!!! getMoveZones() ${it.size}")
                    mapContentRepository.saveData(
                        mapContentRepository.getData().copy(
                            moveZones = it
                        )
                    )
                    changeState(MapUiState.ShowMoveZones(filterMoveZones(it)))
                },
                onError = { Logg.d("!!! ERROR getMoveZones()") },
                callName = "getMoveZones"
            )
        } else {
            mapContentRepository.getData().moveZones?.let {
                changeState(MapUiState.ShowMoveZones(filterMoveZones(it)))
            }
        }
    }

    private fun updateActiveRent(isStart: Boolean) {
        if (isStart && activeRentUpdateTask == null) {
            activeRentUpdateTask = Timer().schedule(0, CHECK_ACTIVE_RENT_DELAY) {
                checkActiveRent()
            }
        } else if (!isStart) {
            activeRentUpdateTask?.cancel()
            activeRentUpdateTask = null
        }
    }

    private fun checkActiveRent() {
        processNetworkCall(
            action = { rentRepository.checkActiveRent() },
            onSuccess = {
                Logg.d("!!!! checkActiveRent found ${it.size}")
                if (it.isNotEmpty()) {
                    changeState(MapUiState.ShowActiveRent(it[0]))
                } else if (isActiveRent){
                    changeState(MapUiState.ShowActiveRent())
                }
                isActiveRent = it.isNotEmpty()
            },
            onError = {
                Logg.d("!!! ERROR checkActiveRent()")
                changeState(MapUiState.ShowActiveRent())
            },
            callName = "checkActiveRent"
        )
    }

    companion object {
        private const val SHOW_CONTENT_ZOOM = 5f
        private const val SHOW_PARKINGS_ZOOM = 16f
        private const val SHOW_SLOW_ZONE_ZOOM = 11f
        private const val SHOW_SLOW_ZONE_MARKER_ZOOM = 14f
        private const val CHECK_RENT_STATUS_DELAY = 3000L
        private const val CHECK_ACTIVE_RENT_DELAY = 10000L
    }
}
