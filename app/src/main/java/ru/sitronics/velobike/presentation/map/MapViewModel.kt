package ru.sitronics.velobike.presentation.map

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.map.MapContentUseCase
import ru.sitronics.velobike.domain.rent.ActiveRent
import ru.sitronics.velobike.domain.rent.ChooseParkingParams
import ru.sitronics.velobike.domain.rent.MainRentStatus
import ru.sitronics.velobike.domain.rent.ProgressStatus
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
    private var showActiveRentBar: Boolean = false
    private var showWheelLock: Boolean = false
    private var chooseParking: Boolean = false

    init {
        rentUseCase.scope = viewModelScope
        mapContentUseCase.scope = viewModelScope
    }

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.ResetState -> {
                changeState(MapUiState.Normal)
            }
            is MapIntent.MapStart -> {
                rentUseCase.onMapStart()

                mapContentUseCase.updateMoveZones({ showError(it) }) { moveZones ->
                    changeState(MapUiState.MoveZones(filterMoveZones(moveZones)))
                }

                rentUseCase.updateActiveRent(true, { showError(it) }) { activeRent ->
                    handleActiveRent(activeRent)
                }
            }
            is MapIntent.MapStop -> {
                rentUseCase.updateActiveRent(false, {}) { _ -> }
            }
            is MapIntent.ChangeMapPosition -> {
                mapContentUseCase.updateMapContent(intent.mapRect, intent.zoom) {
                    Logg.d("!!!! MapIntent.ChangeMapPosition updateMapContent bikes ${it.bikes?.size}")
                    changeState(MapUiState.MapContent(
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
            is MapIntent.CloseBikeDetail -> {
                intent.id?.let { id ->
                    if (intent.fromQrScan) {
                        rentUseCase.startRent(
                            id, intent.latitude, intent.longitude,
                            { showError(it) }
                        ) { activeRent ->
                            changeState(MapUiState.CurrentRent(activeRent, true))
                        }
                    } else {
                        changeState(MapUiState.QrScan(show = true, fromBikeDetail = true))
                    }
                }
            }
            is MapIntent.QrScanTap -> {
                changeState(MapUiState.QrScan(true))
            }
            is MapIntent.CloseQrScan -> {
                viewModelScope.launch {
                    changeState(MapUiState.QrScan(false))
                    delay(CHANGE_STATE_DELAY)
                    intent.id?.let { id ->
                        if (intent.fromBikeDetail) {
                            rentUseCase.startRent(
                                id, intent.latitude, intent.longitude,
                                { showError(it) }
                            ) { activeRent ->
                                changeState(MapUiState.CurrentRent(activeRent, true))
                            }
                        } else {
                            mapContentUseCase.runWithBike(id) { bike ->
                                changeState(MapUiState.BikeDetail(bike, fromQrScan = true))
                            }
                        }
                    }
                }
            }
            is MapIntent.CloseActiveRent -> {
                showActiveRentBar = !intent.isClicked

                if (intent.isClicked) {
                    rentUseCase.finishRent(
                        intent.latitude, intent.longitude,
                        { showError(it) }
                    ) { activeRent ->
                        changeState(MapUiState.FinishingRent(true))
                    }
                } else {
                    changeState(MapUiState.ActiveRentBar(true))
                }
            }
            is MapIntent.CloseFinishingRent -> {
                showActiveRentBar = !intent.isClicked

                if (intent.isClicked) {
                    rentUseCase.activeRent?.let { rent ->
                        rentUseCase.checkRentStatus(
                            rent.rentId, rent.deviceId,
                            { showError(it) }
                        ) {
                            when (it.processStatus) {
                                ProgressStatus.WAIT_CLOSE_LOCK -> {
                                    showWheelLock = true
                                    changeState(MapUiState.WheelLock)
                                }
                                ProgressStatus.WAIT_UPLOAD_PHOTO ->
                                    changeState(MapUiState.TakePhoto)
                                ProgressStatus.WAIT_PARKING_FROM_CLIENT -> {
                                    chooseParking = true
                                    changeState(MapUiState.FinishingRent(false))
                                    delay(CHANGE_STATE_DELAY)
                                    changeState(MapUiState.ChooseParking)
                                }
                                else -> {}
                            }
                        }
                    }
                } else {
                    changeState(MapUiState.ActiveRentBar(true))
                }
            }
            is MapIntent.ClickActiveRentBar -> {
                rentUseCase.activeRent?.let {
                    showActiveRentBar = false
                    viewModelScope.launch {
                        handleActiveRent(it)
                    }
                }
            }
            is MapIntent.CloseChooseParking -> {
                changeState(MapUiState.Normal)

                if (intent.isClicked) {
                    handleChooseParking()
                } else {
                    viewModelScope.launch {
                        delay(60000)
                        chooseParking = false
                    }
                }
            }
            is MapIntent.CloseWheelLock -> {
                showWheelLock = false
                changeState(MapUiState.FinishingRent(true))
            }
            is MapIntent.OnTakePhoto -> {
                if (intent.filePath != null) {
                    // TODO: temp
//                    rentUseCase.updateActiveRent(false, {}) { _ -> }
                    rentUseCase.uploadPhotoAndFinishRent(
                        intent.filePath,
                        { showError(it) }
                    ) {
                        if (it.status?.isDone() == true) {
                            changeState(MapUiState.FinishingRent(false))
                            delay(CHANGE_STATE_DELAY)
                            changeState(MapUiState.FinishedRent(rentUseCase.activeRent))
                        } else
                            showError(context.getString(R.string.error_unknown))
                    }
                } else
                    changeState(MapUiState.FinishingRent(true))
            }
            is MapIntent.CloseFinishedRent -> {
                changeState(MapUiState.Normal)
            }
            is MapIntent.CloseError -> {
                changeState(MapUiState.Normal)
            }
        }
    }

    private fun changeState(uiState: MapUiState) {
        _mapUiState.value = uiState
    }

    private suspend fun handleActiveRent(activeRent: ActiveRent?) {
        if (activeRent == null) showActiveRentBar = false
        var show = !showActiveRentBar && activeRent?.rentStatus == MainRentStatus.IN_PROGRESS
        changeState(MapUiState.CurrentRent(activeRent, show))
        delay(CHANGE_STATE_DELAY)
        show = !showActiveRentBar && !showWheelLock && !chooseParking &&
                activeRent?.rentStatus == MainRentStatus.CHECK_END
        changeState(MapUiState.FinishingRent(show))
        delay(CHANGE_STATE_DELAY)
        changeState(MapUiState.ActiveRentBar(showActiveRentBar))
    }

    private fun onBikeClick(id: String) {
        Logg.d("!!! onBikeClick $id")
        mapContentUseCase.getBike(id)?.let { bike ->
            changeState(MapUiState.BikeDetail(bike))
        }
    }

    private fun onStationClick(id: String) {
        Logg.d("!!! onStationClick $id")
        if (chooseParking)
            handleChooseParking(id)
        else
            mapContentUseCase.getStation(id)?.let { station ->
                changeState(MapUiState.StationDetail(station))
            }
    }

    private fun onParkingClick(id: String) {
        Logg.d("!!! onParkingClick $id")
        mapContentUseCase.getParking(id)?.let { parking ->
            changeState(MapUiState.ParkingDetail(parking))
        }
    }

    private fun handleChooseParking(id: String = UNDEFINED_PARKING) {
        chooseParking = false

        rentUseCase.activeRent?.let { rent ->
            rentUseCase.chooseParking(
                rent.rentId, ChooseParkingParams(rent.deviceId, id),
                { showError(it) }
            ) {
                changeState(MapUiState.FinishingRent(true))
            }
        }
    }

    private fun showError(msg: String?) =
        msg?.let { changeState(MapUiState.Error(context.getString(R.string.error_title), it)) }

    companion object {
        private const val CHANGE_STATE_DELAY = 500L
        private const val UNDEFINED_PARKING = "undefined"
    }
}
