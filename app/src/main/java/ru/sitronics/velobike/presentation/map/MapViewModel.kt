package ru.sitronics.velobike.presentation.map

import android.content.Context
import android.location.Location
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.SHOW_CONTENT_ZOOM
import ru.sitronics.velobike.SHOW_PARKINGS_ZOOM
import ru.sitronics.velobike.SHOW_SLOW_ZONE_MARKER_ZOOM
import ru.sitronics.velobike.SHOW_SLOW_ZONE_ZOOM
import ru.sitronics.velobike.domain.chat.ChatManager
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.MapContentUseCase
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.domain.map.SlowZone
import ru.sitronics.velobike.domain.rent.ChooseParkingParams
import ru.sitronics.velobike.domain.rent.MainRentStatus
import ru.sitronics.velobike.domain.rent.ProgressStatus
import ru.sitronics.velobike.domain.rent.Rent
import ru.sitronics.velobike.domain.rent.RentUseCase
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import ru.sitronics.velobike.tools.filterMoveZones
import ru.sitronics.velobike.tools.mapSlowZones
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val rentUseCase: RentUseCase,
    private val mapContentUseCase: MapContentUseCase,
    private val chatManager: ChatManager,
    appContext: Context,
) : BaseViewModel(appContext) {
    private val _mapUiStates: MutableSharedFlow<MapUiState> = MutableSharedFlow(replay = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val mapUiStates: SharedFlow<MapUiState> = _mapUiStates.asSharedFlow()   // used for testing
    private lateinit var mapUiStatesJob: Job
    private val _mapUiState: MutableStateFlow<MapUiState> = MutableStateFlow(MapUiState.Normal)
    val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()
    private var dialogState: RentDialogState = RentDialogState.NONE
    private var filterType: BikeParkingType = BikeParkingType.ALL
    private var isParkMode: Boolean = false

    init {
        rentUseCase.initScope(viewModelScope)
        mapContentUseCase.initScope(viewModelScope)
        initStates()
    }

    override fun onCleared() {
        super.onCleared()
        rentUseCase.clearScope()
        mapContentUseCase.clearScope()
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

                rentUseCase.updateActiveRent(false,
                    { showError(it) },
                    { handleActiveRent(it) },
                    { changeState(MapUiState.FinishedRent(it)) })

                chatManager.addUnreadMessagesCountListener {
                    changeState(MapUiState.ChatUnreadMessages(it))
                }
            }
            is MapIntent.MapStop -> {
                rentUseCase.updateActiveRent(true, {}, {}, {})

                chatManager.addUnreadMessagesCountListener {}
            }
            is MapIntent.ChangeMapPosition -> {
                Logg.d("!!!! MapIntent.ChangeMapPosition ${intent.mapRect}")

                if (intent.zoom < SHOW_CONTENT_ZOOM) return

/*
                mapContentUseCase.updateMapContent(intent.mapRect, intent.zoom) {
                    changeState(MapUiState.MapContent(
                        bikes = it.bikes,
                        stations = it.stations,
                        parkings = it.parkings,
                        slowZones = filterSlowZones(it.slowZones),
                        showMarkers = it.showMarkers,
                    ))
                }
*/

                mapContentUseCase.updateBikes(
                    intent.mapRect,
                    { bikes -> changeState(MapUiState.Bikes(bikes.filterBikes().toImmutableList())) },
                    { showError(it) }
                )

                mapContentUseCase.updateParkings(
                    intent.mapRect,
                    { stations, parkings -> changeState(MapUiState.Parkings(stations.filterStations().toImmutableList(), parkings.filterParkings(intent.zoom).toImmutableList(), isParkMode)) },
                    { showError(it) }
                )

                mapContentUseCase.updateSlowZones(
                    { slowZones -> changeState(MapUiState.SlowZones(slowZones.filterSlowZones(intent.zoom).mapSlowZones().toImmutableList(), intent.zoom >= SHOW_SLOW_ZONE_MARKER_ZOOM)) },
                    { showError(it) }
                )
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
            is MapIntent.MapFilterTap -> {
                filterType = intent.type
                changeState(MapUiState.Bikes(mapContentUseCase.getBikes().filterBikes().toImmutableList()))
                changeState(MapUiState.Parkings(mapContentUseCase.getStations().filterStations().toImmutableList(),
                                                mapContentUseCase.getParkings().filterParkings(intent.zoom).toImmutableList(), isParkMode))
            }
            is MapIntent.ChangeParkMode -> {
                isParkMode = !isParkMode
                changeState(MapUiState.Parkings(mapContentUseCase.getStations().filterStations().toImmutableList(),
                                                mapContentUseCase.getParkings().filterParkings(intent.zoom).toImmutableList(), isParkMode))
            }
            is MapIntent.ChatTap -> {
                chatManager.showChat(intent.context)
            }
            is MapIntent.Search -> {
                handleSearch(intent)
            }
            is MapIntent.SearchAction -> {
                handleSearchAction(intent)
            }
            is MapIntent.ScanQrTap -> {
                changeState(MapUiState.QrScan(true))
            }
            is MapIntent.ScanQrAction -> {
                handleScanQrAction(intent)
            }
            is MapIntent.BikeDetailAction -> {
                handleBikeDetailAction(intent)
            }
            is MapIntent.ActiveRentAction -> {
                handleActiveRentAction(intent)
            }
            is MapIntent.FinishingRentAction -> {
                handleFinishingRentAction(intent)
            }
            is MapIntent.ClickActiveRentBar -> {
                rentUseCase.rent?.let {
                    changeState(MapUiState.CloseAllDetails)
                    dialogState = RentDialogState.NONE
                    handleActiveRent(it)
                }
            }
            is MapIntent.ChooseParkingAction -> {
                handleChooseParkingAction(intent)
            }
            is MapIntent.WheelLockAction -> {
                dialogState = RentDialogState.NONE
                changeState(MapUiState.FinishingRent(true))
            }
            is MapIntent.TakePhotoAction -> {
                handleTakePhotoAction(intent)
            }
            is MapIntent.FinishedRentAction -> {
                handleFinishedRentAction(intent)
            }
            is MapIntent.ErrorAction -> {
                changeState(MapUiState.Normal)
            }
        }
    }

    private fun List<Bike>.filterBikes() =
        if ((filterType == BikeParkingType.ALL || filterType == BikeParkingType.ELECTRO_2_0) && !isParkMode) this
        else emptyList()

    private fun List<Parking>.filterParkings(zoom: Float) =
        if ((filterType == BikeParkingType.ALL || filterType == BikeParkingType.ELECTRO_2_0) && zoom >= SHOW_PARKINGS_ZOOM) this
        else emptyList()

    private fun List<Parking>.filterStations() =
        when (filterType) {
            BikeParkingType.ALL -> this
            BikeParkingType.ELECTRICAL -> this.filter { it.availableElectricBikes > 0 }
            BikeParkingType.MECHANICAL -> this.filter { it.availableElectricBikes == 0 }
            else -> emptyList()
        }

    private fun List<SlowZone>.filterSlowZones(zoom: Float) =
        if (zoom >= SHOW_SLOW_ZONE_ZOOM) this
        else emptyList()

    private fun showBikeDetail(id: String) {
        Logg.d("!!! showBikeDetail $id")
        mapContentUseCase.getBike(id)?.let { bike ->
            changeState(MapUiState.BikeDetail(bike, false, rentUseCase.rent == null))
        }
    }

    private fun showStationDetail(id: String) {
        Logg.d("!!! showStationDetail $id")
        if (dialogState == RentDialogState.CHOOSE_PARKING)
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

    private fun handleSearch(intent: MapIntent.Search) {
        val parkings = if (intent.searchStr.length > 1) mapContentUseCase.findParking(intent.searchStr)
                       else emptyList()
        // calculate distance
        val isLocation = intent.latitude != null && intent.longitude != null
        val res = FloatArray(1)
        parkings.forEach {
            if (isLocation)
                Location.distanceBetween(
                    intent.latitude!!, intent.longitude!!,
                    it.latitude, it.longitude, res
                )
            it.distance = if (isLocation) res[0] else null
        }
        dialogState = RentDialogState.SEARCH
        handleActiveRent(rentUseCase.rent)
        changeState(MapUiState.Search(parkings))
    }

    private fun handleSearchAction(intent: MapIntent.SearchAction) {
        dialogState = if (rentUseCase.rent != null) RentDialogState.ACTIVE_RENT_BAR
                      else RentDialogState.NONE
        intent.id?.let {
            showStationDetail(it)
        }
    }

    private fun handleScanQrAction(intent: MapIntent.ScanQrAction) {
        changeState(MapUiState.QrScan(false))

        when (intent.action) {
            DialogAction.CLICK -> {
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
                            changeState(MapUiState.BikeDetail(bike, true, rentUseCase.rent == null))
                        }
                    }
                }
            }

            DialogAction.DISMISS -> {
                changeState(MapUiState.Normal)
            }

            else -> {}
        }
    }

    private fun handleBikeDetailAction(intent: MapIntent.BikeDetailAction) {
        when (intent.action) {
            DialogAction.CLICK -> {
                intent.id?.let { id ->
                    if (intent.fromQrScan) {
                        changeState(MapUiState.Loading(true))
                        rentUseCase.startRent(
                            id, intent.latitude, intent.longitude,
                            { showError(it) }
                        ) {
                            changeState(MapUiState.Loading(false))
                            handleActiveRent(it)
                        }
                    } else {
                        changeState(MapUiState.QrScan(show = true, fromBikeDetail = true))
                    }
                }
            }

            DialogAction.DISMISS -> {
                changeState(MapUiState.Normal)
            }

            else -> {}
        }
    }

    private fun getNearestStation(lat: Double?, lon: Double?) : Parking? {
        if (lat == null || lon == null) return null
        val res = FloatArray(1)

        return mapContentUseCase.getStations().filter {
            !it.isLocked && it.freeNonElectricSlots + it.freeElectricSlots > 0
        }.minByOrNull {
            Location.distanceBetween(lat, lon, it.latitude, it.longitude, res)
            if (res[0] != 0f) res[0] else Float.MAX_VALUE
        }
    }

    private fun handleActiveRentAction(intent: MapIntent.ActiveRentAction) {
        when (intent.action) {
            DialogAction.CLICK -> {
                rentUseCase.rent?.let {
                    if (!it.isOld) {
                        changeState(MapUiState.Loading(true))
                        rentUseCase.finishRent(
                            intent.latitude, intent.longitude,
                            { showError(it) }
                        ) {
                            changeState(MapUiState.Loading(false))
                            handleActiveRent(it)
                        }
                    } else {
                        getNearestStation(intent.latitude, intent.longitude)?.let { station ->
                            dialogState = RentDialogState.ACTIVE_RENT_BAR
                            changeState(MapUiState.ActiveRentBar(true))
                            changeState(MapUiState.StationDetail(station))
                        }
                    }
                }
            }

            DialogAction.BACK -> {
                changeState(MapUiState.Loading(true))
                rentUseCase.unlockWheel({ showError(it) }) {
                    changeState(MapUiState.Loading(false))
                }
            }

            DialogAction.DISMISS -> {
                dialogState = RentDialogState.ACTIVE_RENT_BAR
                changeState(MapUiState.ActiveRentBar(true))
            }
        }
    }

    private fun handleActiveRent(rent: Rent?) {
        if (rent == null && dialogState == RentDialogState.ACTIVE_RENT_BAR)
            dialogState = RentDialogState.NONE
        var show = dialogState.isNone() && rent?.rentStatus == MainRentStatus.IN_PROGRESS
        changeState(MapUiState.ActiveRent(rent, show))
        show = dialogState.isNone() && rent?.rentStatus == MainRentStatus.CHECK_END
        changeState(MapUiState.FinishingRent(show))
        changeState(MapUiState.ActiveRentBar(dialogState == RentDialogState.ACTIVE_RENT_BAR))
        changeState(MapUiState.QrScanButton(rent == null))
    }

    private fun handleFinishingRentAction(intent: MapIntent.FinishingRentAction) {
        rentUseCase.rent?.let { rent ->
            when (intent.action) {
                DialogAction.CLICK -> {
                    changeState(MapUiState.Loading(true))
                    rentUseCase.checkRentStatus(
                        rent.rentId, rent.frameNumber,
                        { showError(it) }
                    ) { status ->
                        changeState(MapUiState.Loading(false))

                        when (status.processStatus) {
                            ProgressStatus.WAIT_PARKING_FROM_CLIENT -> {
                                dialogState = RentDialogState.CHOOSE_PARKING
                                changeState(MapUiState.FinishingRent(false))
                                changeState(MapUiState.ChooseParking)
                            }

                            ProgressStatus.WAIT_CLOSE_LOCK -> {
                                dialogState = RentDialogState.WHEEL_LOCK
                                changeState(MapUiState.WheelLock)
                            }

                            ProgressStatus.WAIT_UPLOAD_PHOTO ->
                                changeState(MapUiState.TakePhoto)

                            ProgressStatus.PHOTO_WAS_UPLOADED -> {
                                changeState(MapUiState.Loading(true))
                                rentUseCase.finishRentAfterUploadPhoto({ showError(it) }) {
                                    if (it.status?.isDone() == true) {
                                        changeState(MapUiState.FinishingRent(false), true)
                                        changeState(MapUiState.FinishedRent(rentUseCase.rent))
                                    } else
                                        showError(appContext.getString(R.string.error_unknown))
                                }
                            }

                            else -> {}
                        }
                    }
                }

                DialogAction.BACK -> {
                    changeState(MapUiState.Loading(true))
                    rentUseCase.returnToActiveRent(
                        rent.rentId, { showError(it) }
                    ) {
                        changeState(MapUiState.Loading(false))
                        handleActiveRent(it)
                    }
                }

                DialogAction.DISMISS -> {
                    dialogState = RentDialogState.ACTIVE_RENT_BAR
                    changeState(MapUiState.ActiveRentBar(true))
                }
            }
        }
    }

    private fun handleChooseParkingAction(intent: MapIntent.ChooseParkingAction) {
        if (intent.action == DialogAction.CLICK) {
            handleChooseParking()
        } else {
            changeState(MapUiState.Normal)
            viewModelScope.launch {
                delay(CHOOSE_PARKING_TIME)
                dialogState = RentDialogState.NONE
            }
        }
    }

    private fun handleChooseParking(id: String = UNDEFINED_PARKING) {
        dialogState = RentDialogState.NONE

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

    private fun handleTakePhotoAction(intent: MapIntent.TakePhotoAction) {
        when (intent.action) {
            DialogAction.CLICK -> {
                if (intent.filePath.isNotEmpty()) {
                    changeState(MapUiState.Loading(true))
                    rentUseCase.uploadPhotoAndFinishRent(
                        intent.filePath,
                        { showError(it) }
                    ) {
                        if (it.status?.isDone() == true) {
                            changeState(MapUiState.FinishingRent(false), true)
                            changeState(MapUiState.FinishedRent(rentUseCase.rent))
                        } else
                            showError(appContext.getString(R.string.error_unknown))
                    }
                }
            }

            DialogAction.DISMISS -> {
                changeState(MapUiState.FinishingRent(true))
            }

            else -> {}
        }
    }

    private fun handleFinishedRentAction(intent: MapIntent.FinishedRentAction) {
        when (intent.action) {
            DialogAction.CLICK -> {
                changeState(MapUiState.Loading(true))
                rentUseCase.sendFeedback(
                    intent.rent, intent.rating ?: 0, { showError(it) }
                ) {
                    changeState(MapUiState.Normal, true)
                }
            }

            DialogAction.DISMISS -> {
                changeState(MapUiState.Normal)
            }

            else -> {}
        }
    }

    private fun showError(msg: String?) {
        changeState(MapUiState.Loading(false))
        msg?.let { changeState(MapUiState.Error(appContext.getString(R.string.error_title), it)) }
    }

    private fun initStates() {
        mapUiStatesJob = _mapUiStates.onEach {
            delay(CHANGE_STATE_DELAY)
            _mapUiState.value = it
        }.launchIn(viewModelScope)
    }

    private fun closeStates() {
        mapUiStatesJob.cancel()
    }

    private fun changeState(uiState: MapUiState, stopLoading: Boolean = false) {
        if (stopLoading)
            _mapUiStates.tryEmit(MapUiState.Loading(false))
        _mapUiStates.tryEmit(uiState)
    }

    companion object {
        private const val CHANGE_STATE_DELAY = 50L
        private const val CHOOSE_PARKING_TIME = 60000L
        private const val UNDEFINED_PARKING = "undefined"
    }
}
