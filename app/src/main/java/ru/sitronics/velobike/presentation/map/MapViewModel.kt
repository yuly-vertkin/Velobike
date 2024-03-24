package ru.sitronics.velobike.presentation.map

import android.location.Location
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.chat.ChatManager
import ru.sitronics.velobike.domain.map.MapContentUseCase
import ru.sitronics.velobike.domain.rent.ChooseParkingParams
import ru.sitronics.velobike.domain.rent.MainRentStatus
import ru.sitronics.velobike.domain.rent.ProgressStatus
import ru.sitronics.velobike.domain.rent.Rent
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
    private val chatManager: ChatManager,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val mapUiStates: MutableSharedFlow<MapUiState> = MutableSharedFlow(replay = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private lateinit var mapUiStatesJob: Job
    private val _mapUiState: MutableStateFlow<MapUiState> = MutableStateFlow(MapUiState.Normal)
    val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()
    private var dialogState: MapDialogState = MapDialogState.NONE

    init {
        rentUseCase.initScope(viewModelScope)
        mapContentUseCase.initScope(viewModelScope)
        initStates()
    }

    override fun onCleared() {
        super.onCleared()
        closeStates()
    }

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.ResetState -> {
                changeState(MapUiState.Normal)
            }
            is MapIntent.MapStart -> {
                mapContentUseCase.updateMoveZones({ showError(it) }) { moveZones ->
                    changeState(MapUiState.MoveZones(filterMoveZones(moveZones)))
                }

                rentUseCase.updateActiveRent(true,
                    { showError(it) },
                    { handleActiveRent(it) },
                    { changeState(MapUiState.FinishedRent(it)) })

                chatManager.addUnreadMessagesCountListener {
                    changeState(MapUiState.ChatUnreadMessages(it))
                }
            }
            is MapIntent.MapStop -> {
                rentUseCase.updateActiveRent(false, {}, {}) { _ -> }

                chatManager.addUnreadMessagesCountListener {}
            }
            is MapIntent.ChangeMapPosition -> {
                Logg.d("!!!! MapIntent.ChangeMapPosition ${intent.mapRect}")
                mapContentUseCase.updateMapContent(intent.mapRect, intent.zoom) {
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
                    is MarkerUserData.Bike -> showBikeDetail(intent.userData.id)
                    is MarkerUserData.Station -> showStationDetail(intent.userData.id)
                    is MarkerUserData.Parking -> showParkingDetail(intent.userData.id)
                    is MarkerUserData.SlowZone -> {}//onSlowZoneClick(data)
                    is MarkerUserData.MoveZone -> {}//onNotMoveZoneClick()
                    null -> {}
                }
            }
            is MapIntent.BikeDetailAction -> {
                intent.id?.let { id ->
                    if (intent.fromQrScan) {
                        changeState(MapUiState.Loading(true))
                        rentUseCase.startRent(
                            id, intent.latitude, intent.longitude,
                            { showError(it) }
                        ) {
                            changeState(MapUiState.ActiveRent(it, true), true)
                        }
                    } else {
                        changeState(MapUiState.QrScan(show = true, fromBikeDetail = true))
                    }
                }
            }
            is MapIntent.QrScanTap -> {
                changeState(MapUiState.QrScan(true))
            }
            is MapIntent.QrScanAction -> {
                changeState(MapUiState.QrScan(false))
                intent.id?.let { id ->
                    if (intent.fromBikeDetail) {
                        changeState(MapUiState.Loading(true))
                        rentUseCase.startRent(
                            id, intent.latitude, intent.longitude,
                            { showError(it) }
                        ) {
                            changeState(MapUiState.ActiveRent(it, true), true)
                        }
                    } else {
                        mapContentUseCase.runWithBike(id) { bike ->
                            changeState(MapUiState.BikeDetail(bike, fromQrScan = true))
                        }
                    }
                }
            }
            is MapIntent.Search -> {
                val parkings = if (intent.searchStr.length > 1) mapContentUseCase.findParking(intent.searchStr)
                               else emptyList()
                // calculate distance
                val isLocation = intent.latitude != null && intent.longitude != null
                val res = FloatArray(1)
                parkings.forEach {
                    if (isLocation)
                        Location.distanceBetween(intent.latitude!!, intent.longitude!!, it.latitude, it.longitude, res)
                    it.distance = if (isLocation) res[0] else null
                }
                dialogState = MapDialogState.SEARCH
                handleActiveRent(rentUseCase.rent)
                changeState(MapUiState.Search(parkings))
            }
            is MapIntent.SearchAction -> {
                dialogState = MapDialogState.ACTIVE_RENT_BAR
                intent.id?.let {
                    showStationDetail(it)
                }
            }
            is MapIntent.ActiveRentAction -> {
                if (intent.isClicked) {
                    changeState(MapUiState.Loading(true))
                    rentUseCase.finishRent(
                        intent.latitude, intent.longitude,
                        { showError(it) }
                    ) {
                        changeState(MapUiState.FinishingRent(true), true)
                    }
                } else {
                    dialogState = MapDialogState.ACTIVE_RENT_BAR
                    changeState(MapUiState.ActiveRentBar(true))
                }
            }
            is MapIntent.FinishingRentAction -> {
                handleFinishingRentAction(intent.action)
            }
            is MapIntent.ClickActiveRentBar -> {
                rentUseCase.rent?.let {
                    dialogState = MapDialogState.NONE
                    viewModelScope.launch {
                        handleActiveRent(it)
                    }
                }
            }
            is MapIntent.ChooseParkingAction -> {
                if (intent.isClicked) {
                    handleChooseParking()
                } else {
                    changeState(MapUiState.Normal)
                    viewModelScope.launch {
                        delay(CHOOSE_PARKING_TIME)
                        dialogState = MapDialogState.NONE
                    }
                }
            }
            is MapIntent.WheelLockAction -> {
                dialogState = MapDialogState.NONE
                changeState(MapUiState.FinishingRent(true))
            }
            is MapIntent.OnTakePhoto -> {
                if (intent.filePath != null) {
                    changeState(MapUiState.Loading(true))
                    rentUseCase.uploadPhotoAndFinishRent(
                        intent.filePath,
                        { showError(it) }
                    ) {
                        if (it.status?.isDone() == true) {
                            changeState(MapUiState.FinishingRent(false), true)
                        } else
                            showError(context.getString(R.string.error_unknown))
                    }
                } else
                    changeState(MapUiState.FinishingRent(true))
            }
            is MapIntent.FinishedRentAction -> {
                rentUseCase.sendFeedback(intent.rent, intent.rating, { showError(it) }) {
                    changeState(MapUiState.Normal)
                }
            }
            is MapIntent.ErrorAction -> {
                changeState(MapUiState.Normal)
            }
            is MapIntent.ChatTap -> {
                chatManager.showChat(intent.context)
            }
        }
    }

    private fun handleFinishingRentAction(action: DialogAction) {
        rentUseCase.rent?.let { rent ->
            when (action) {
                DialogAction.CLICK -> {
                    changeState(MapUiState.Loading(true))
                    rentUseCase.checkRentStatus(
                        rent.rentId, rent.frameNumber,
                        { showError(it) }
                    ) {
                        changeState(MapUiState.Loading(false))

                        when (it.processStatus) {
                            ProgressStatus.WAIT_PARKING_FROM_CLIENT -> {
                                dialogState = MapDialogState.CHOOSE_PARKING
                                changeState(MapUiState.FinishingRent(false))
                                changeState(MapUiState.ChooseParking)
                            }

                            ProgressStatus.WAIT_CLOSE_LOCK -> {
                                dialogState = MapDialogState.WHEEL_LOCK
                                changeState(MapUiState.WheelLock)
                            }

                            ProgressStatus.WAIT_UPLOAD_PHOTO ->
                                changeState(MapUiState.TakePhoto)

                            else -> {}
                        }
                    }
                }

                DialogAction.BACK -> {
                    changeState(MapUiState.Loading(true))
                    rentUseCase.returnToActiveRent(
                        rent.rentId, { showError(it) }
                    ) {
                        changeState(MapUiState.ActiveRent(rent, true), true)
                    }
                }

                else -> {
                    dialogState = MapDialogState.ACTIVE_RENT_BAR
                    changeState(MapUiState.ActiveRentBar(true))
                }
            }
        }
    }

    private fun handleActiveRent(rent: Rent?) {
        if (rent == null && dialogState == MapDialogState.ACTIVE_RENT_BAR)
            dialogState = MapDialogState.NONE
        var show = dialogState.isNone() && rent?.rentStatus == MainRentStatus.IN_PROGRESS
        changeState(MapUiState.ActiveRent(rent, show))
        show = dialogState.isNone() && rent?.rentStatus == MainRentStatus.CHECK_END
        changeState(MapUiState.FinishingRent(show))
        changeState(MapUiState.ActiveRentBar(dialogState == MapDialogState.ACTIVE_RENT_BAR))
        changeState(MapUiState.QrScanButton(rent == null))
    }

    private fun showBikeDetail(id: String) {
        Logg.d("!!! showBikeDetail $id")
        mapContentUseCase.getBike(id)?.let { bike ->
            changeState(MapUiState.BikeDetail(bike))
        }
    }

    private fun showStationDetail(id: String) {
        Logg.d("!!! showStationDetail $id")
        if (dialogState == MapDialogState.CHOOSE_PARKING)
            handleChooseParking(id)
        else
            mapContentUseCase.getStation(id)?.let { station ->
                changeState(MapUiState.StationDetail(station))
            }
    }

    private fun showParkingDetail(id: String) {
        Logg.d("!!! showParkingDetail $id")
        mapContentUseCase.getParking(id)?.let { parking ->
            changeState(MapUiState.ParkingDetail(parking))
        }
    }

    private fun handleChooseParking(id: String = UNDEFINED_PARKING) {
        dialogState = MapDialogState.NONE

        rentUseCase.rent?.let { rent ->
            changeState(MapUiState.Loading(true))
            rentUseCase.chooseParking(
                rent.rentId, ChooseParkingParams(id),
                { showError(it) }
            ) {
                changeState(MapUiState.FinishingRent(true), true)
            }
        }
    }

    private fun showError(msg: String?) {
        changeState(MapUiState.Loading(false))
        msg?.let { changeState(MapUiState.Error(context.getString(R.string.error_title), it)) }
    }

    private fun initStates() {
        mapUiStatesJob = mapUiStates.onEach {
            delay(CHANGE_STATE_DELAY)
            _mapUiState.value = it
        }.launchIn(viewModelScope)
    }

    private fun closeStates() {
        mapUiStatesJob.cancel()
    }

    private fun changeState(uiState: MapUiState, stopLoading: Boolean = false) {
        if (stopLoading)
            mapUiStates.tryEmit(MapUiState.Loading(false))
        mapUiStates.tryEmit(uiState)
    }

    companion object {
        private const val CHANGE_STATE_DELAY = 50L
        private const val CHOOSE_PARKING_TIME = 60000L
        private const val UNDEFINED_PARKING = "undefined"
    }
}
