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
import ru.sitronics.velobike.domain.rent.FailedReason
import ru.sitronics.velobike.domain.rent.MainRentStatus
import ru.sitronics.velobike.domain.rent.RentRepository
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import ru.sitronics.velobike.tools.filterMoveZones
import ru.sitronics.velobike.tools.filterSlowZones
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapContentRepository: MapContentRepository,
    private val rentRepository: RentRepository,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _mapUiState: MutableStateFlow<MapUiState> = MutableStateFlow(MapUiState.Normal)
    val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()
    private var prevMapUiState: MapUiState = MapUiState.Normal
    private var rentStatus: MainRentStatus? = null
    private var showSlowZones: Boolean = false
    private var showSlowZoneMarkers: Boolean = false

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.MapStart -> {
                updateMoveZones()
            }
            is MapIntent.ChangeMapPosition -> {
                updateBikesAndParkings(intent.mapRect, intent.zoom)
                updateSlowZones(intent.zoom)
            }
            is MapIntent.MapObjectTap -> {
                when (intent.userData) {
                    is MarkerUserData.Bike -> onBikeClick(intent.userData.id)
                    is MarkerUserData.Station -> onParkingClick(intent.userData.id)
                    is MarkerUserData.Parking -> onParkingClick(intent.userData.id)
                    is MarkerUserData.SlowZone -> {}//onSlowZoneClick(data)
                    is MarkerUserData.MoveZone -> {}//onNotMoveZoneClick()
                    null -> {}
                }
            }
            is MapIntent.CloseBikeDetail -> {
                if (prevMapUiState is MapUiState.ShowQrScan && intent.bikeId != null) {
                    startRent(intent.bikeId, intent.latitude, intent.longitude)
                } else {
                    changeState(if (intent.bikeId != null) MapUiState.ShowQrScan else MapUiState.Normal)
                }
            }
            is MapIntent.QrScanTap -> {
                changeState(MapUiState.ShowQrScan)
            }
            is MapIntent.CloseQrScan -> {
                if (prevMapUiState is MapUiState.ShowBikeDetail && intent.bikeId != null) {
                    startRent(intent.bikeId, intent.latitude, intent.longitude)
                } else if (intent.bikeId != null) {
                    runWithBike(intent.bikeId) { bike ->
                        changeState(MapUiState.ShowBikeDetail(bike))
                    }
                } else {
                    changeState(MapUiState.Normal)
                }
            }
            is MapIntent.CloseError -> {
                changeState(MapUiState.Normal)
            }
        }
    }

    private fun changeState(uiState: MapUiState) {
        prevMapUiState = _mapUiState.value
        _mapUiState.value = uiState
    }

    private fun startRent(bikeId: String, latitude: Double?, longitude: Double?) {
        processNetworkCall(
            action = { rentRepository.startRent(bikeId, latitude ?: 0.0, longitude ?: 0.0) },
            onSuccess = {
                Logg.d("!!!! startRent success, status ${it.status}")
                rentStatus = it.status
                delay(CHECK_RENT_STATUS_DELAY)

                while (rentStatus == MainRentStatus.CHECK_START) {
                    checkRentStatus(it.id, it.deviceId ?: "")
                    delay(CHECK_RENT_STATUS_DELAY)
                }
                Logg.d("!!!! startRent end, status $rentStatus")
                changeState(
                    if (rentStatus == MainRentStatus.IN_PROGRESS) MapUiState.Normal
                    else MapUiState.ShowError(getRentError(it.failedReason, true))
                )
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
                rentStatus = it.status
            },
            onError = {
                Logg.d("!!! ERROR checkRentStatus()")
                rentStatus = MainRentStatus.ERROR_START
            },
        )
    }

    private fun getRentError(failedReason: FailedReason?, startRent: Boolean) : String {
        return failedReason?.let {
            context.getString( if (startRent) it.messageIdStart else it.messageIdFinish)
        } ?: context.getString(R.string.start_omni_failed_default)
    }

    private fun onBikeClick(bikeId: String) {
        println("!!! onBikeClick $bikeId")
        getBike(bikeId)?.let { bike ->
            changeState(MapUiState.ShowBikeDetail(bike))
        }
    }

    private fun getBike(bikeId: String) : Bike? =
        mapContentRepository.getData().bikes?.find { it.id == bikeId }

    private fun runWithBike(bikeId: String, action: (Bike) -> Unit) {
        println("!!!! runWithBike: $bikeId")
//        val bike = mapContentRepository.getData().bikes?.find { it.id == bikeId }
        val bike = getBike(bikeId)
        if (bike != null) {
            println("!!!! found Bike: $bikeId")
            action(bike)
        } else {
            println("!!!! runWithBike processNetworkCall $bikeId")
            processNetworkCall(
                action = { mapContentRepository.getBike(bikeId) },
                onSuccess = {
                    Logg.d("!!!! getBike ${it.id}")
                    action(it)
                },
                onError = { Logg.d("!!!! ERROR getBike") },
            )
        }
    }

    private fun onParkingClick(id: String) {
        println("!!! onParkingClick $id")
    }

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

    companion object {
        private const val SHOW_CONTENT_ZOOM = 5f
        private const val SHOW_PARKINGS_ZOOM = 16f
        private const val SHOW_SLOW_ZONE_ZOOM = 11f
        private const val SHOW_SLOW_ZONE_MARKER_ZOOM = 14f
        private const val CHECK_RENT_STATUS_DELAY = 3000L
    }
}
