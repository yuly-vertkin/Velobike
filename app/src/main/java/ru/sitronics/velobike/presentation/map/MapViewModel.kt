package ru.sitronics.velobike.presentation.map

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.map.MapContentUseCase
import ru.sitronics.velobike.domain.rent.RentUseCase
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import ru.sitronics.velobike.tools.filterMoveZones
import ru.sitronics.velobike.tools.filterSlowZones
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val rentUseCase: RentUseCase,
    private val mapContentUseCase: MapContentUseCase,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _mapUiState: MutableStateFlow<MapUiState> = MutableStateFlow(MapUiState.Normal)
    val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()
    private var prevMapUiState: MapUiState = MapUiState.Normal

    init {
        rentUseCase.scope = viewModelScope
        mapContentUseCase.scope = viewModelScope
    }

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.MapStart -> {
                mapContentUseCase.updateMoveZones(
                    { moveZones -> changeState(MapUiState.ShowMoveZones(filterMoveZones(moveZones))) },
                    { showError(it) }
                )

                rentUseCase.updateActiveRent(true,
                    { activeRent ->
                        changeState(MapUiState.ShowActiveRent(activeRent))
// TODO: не знаю надо ли это
//                        activeRent?.let {
//                            delay(1000)
//                            changeState(MapUiState.Normal)
//                        }
                    },
                    { showError(it) }
                )
            }
            is MapIntent.MapStop -> {
                rentUseCase.updateActiveRent(false, {}, {})
            }
            is MapIntent.ChangeMapPosition -> {
                mapContentUseCase.updateMapContent(intent.mapRect, intent.zoom) {
                    changeState(MapUiState.MapContentUpdate(
                        bikes = it.bikes,
                        stations = it.stations,
                        parkings = it.parkings,
                        slowZones = filterSlowZones(it.slowZones),
                        showMarkers = it.showMarkers,
                    ))
                }
/*
                mapContentUseCase.updateBikes(
                    intent.mapRect, intent.zoom,
                    { changeState(MapUiState.BikesUpdated(it)) },
                    { showError(it) }
                )

                mapContentUseCase.updateParkings(
                    intent.mapRect, intent.zoom,
                    { stations, parkings -> changeState(MapUiState.ParkingsUpdated(stations, parkings)) },
                    { showError(it) }
                )

                mapContentUseCase.updateSlowZones(
                    intent.zoom,
                    { slowZones, showMarkers -> changeState(MapUiState.ShowSlowZones(filterSlowZones(slowZones), showMarkers)) },
                    { showError(it) }
                )
*/
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
                    rentUseCase.startRent(
                        intent.id, intent.latitude, intent.longitude,
                        { changeState(MapUiState.ShowActiveRent(it)) },
                        { showError(it) }
                    )
                } else {
                    changeState(if (intent.id != null) MapUiState.ShowQrScan else MapUiState.Normal)
                }
            }
            is MapIntent.QrScanTap -> {
                changeState(MapUiState.ShowQrScan)
            }
            is MapIntent.CloseQrScan -> {
                if (prevMapUiState is MapUiState.ShowBikeDetail && intent.id != null) {
                    rentUseCase.startRent(
                        intent.id, intent.latitude, intent.longitude,
                        { changeState(MapUiState.ShowActiveRent(it)) },
                        { showError(it) }
                    )
                } else if (intent.id != null) {
                    mapContentUseCase.runWithBike(intent.id) { bike ->
                        changeState(MapUiState.ShowBikeDetail(bike))
                    }
                } else {
                    changeState(MapUiState.Normal)
                }
            }
            is MapIntent.CloseError -> {
                changeState(MapUiState.Normal)
            }
            is MapIntent.ActiveRentAction -> {
                if (intent.finishRent) {
                    // TODO: temp
//                    rentUseCase.updateActiveRent(false, {}, {})

                    rentUseCase.finishRent(intent.latitude, intent.longitude,
                        { changeState(MapUiState.ShowWheelLock) },
                        { showError(it) }
                    )
                    // TODO: continue finish rent
                } else {
                    changeState(MapUiState.Normal)
                }
                rentUseCase.isActiveRentClosed = intent.isClosed
            }
            is MapIntent.CloseWheelLock -> {
                changeState(MapUiState.Normal)
            }
        }
    }

    private fun changeState(uiState: MapUiState) {
        prevMapUiState = _mapUiState.value
        _mapUiState.value = uiState
    }

    private fun onBikeClick(id: String) {
        Logg.d("!!! onBikeClick $id")
        mapContentUseCase.getBike(id)?.let { bike ->
            changeState(MapUiState.ShowBikeDetail(bike))
        }
    }

    private fun onStationClick(id: String) {
        Logg.d("!!! onStationClick $id")
        mapContentUseCase.getStation(id)?.let { station ->
            changeState(MapUiState.ShowStationDetail(station))
        }
    }

    private fun onParkingClick(id: String) {
        Logg.d("!!! onParkingClick $id")
        mapContentUseCase.getParking(id)?.let { parking ->
            changeState(MapUiState.ShowParkingDetail(parking))
        }
    }

    private fun showError(msg: String?) =
        msg?.let { changeState(MapUiState.ShowError(it)) }
}
