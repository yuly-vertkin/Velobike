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
import ru.sitronics.velobike.domain.content.SlowZone
import ru.sitronics.velobike.domain.rent.FailedReason
import ru.sitronics.velobike.domain.rent.MainRentStatus
import ru.sitronics.velobike.domain.rent.RentRepository
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import ru.sitronics.velobike.tools.coordinatesToPolygon
import ru.sitronics.velobike.tools.getSlowZoneMarkerPoint
import java.util.Calendar
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

    fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.MapStart -> {
                getSlowZones()
            }
            is MapIntent.ChangeMapPosition -> {
                if (intent.zoom >= SHOW_CONTENT_ZOOM)
                    updateBikesAndParkings(intent.mapRect)
            }
            is MapIntent.MapObjectTap -> {
                when (intent.userData) {
                    is MarkerUserData.Bike -> onBikeClick(intent.userData.id)
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

    private fun updateBikesAndParkings(mapRect: MapRect) {
//        Logg.d("!!! mapRect: ${mapRect.startLat}, ${mapRect.startLong}, ${mapRect.endLat}, ${mapRect.endLong}")
        processNetworkCall(
            action = { mapContentRepository.getBikes(mapRect) },
            onSuccess = { bikes ->
                Logg.d("!!! getBikes() ${bikes.size}")
                mapContentRepository.saveData(mapContentRepository.getData().copy(
                    bikes = bikes
                ))
                val markers = bikes.map { Marker(it.id, it.latitude, it.longitude, MarkerUserData.Bike(it.id)) }
                changeState(MapUiState.BikesUpdated(markers))
            },
            onError = { Logg.d("!!! ERROR getBikes()") },
            force = true,
            callName = "getBikes"
        )

        processNetworkCall(
            action = { mapContentRepository.getParkings(mapRect) },
            onSuccess = { parkings ->
                Logg.d("!!! getParkings() ${parkings.size}")
                mapContentRepository.saveData(mapContentRepository.getData().copy(
                    parkings = parkings
                ))
                val markers = parkings.map { Marker(it.id, it.latitude, it.longitude, MarkerUserData.Parking(it.id)) }
                changeState(MapUiState.ParkingsUpdated(markers))
            },
            onError = { Logg.d("!!! ERROR getParkings() ${it.message}") },
            force = true,
            callName = "getParkings"
        )
    }

    private fun getSlowZones() {
        if (mapContentRepository.getData().slowZones == null) {
            processNetworkCall(
                action = { mapContentRepository.getSlowZones() },
                onSuccess = {
                    Logg.d("!!! getSlowZones() ${it.size}")
                    mapContentRepository.saveData(
                        mapContentRepository.getData().copy(
                            slowZones = it
                        )
                    )
                    changeState(MapUiState.ShowSlowZones(filterSlowZones(it)))
                },
                onError = { Logg.d("!!! ERROR getSlowZones()") },
                callName = "getSlowZones"
            )
        } else {
            mapContentRepository.getData().slowZones?.let {
                changeState(MapUiState.ShowSlowZones(filterSlowZones(it)))
            }
        }
    }

    private fun filterSlowZones(zones: List<SlowZone>) : List<SlowZoneObject> {
        val calendar = Calendar.getInstance()
        val currentDay = calendar[Calendar.DAY_OF_WEEK]
        val currentSecconds = calendar.get(Calendar.SECOND)
        val result = mutableListOf<SlowZoneObject>()

        zones.forEach {
            val polygon = coordinatesToPolygon(it.geomGeo.coordinates)
            val markerPoint = getSlowZoneMarkerPoint(polygon.outerRing.points)
            when (currentDay) {
                Calendar.MONDAY -> {
                    if (currentSecconds > it.mondayStart && currentSecconds < it.mondayEnd) {
                        result.add(SlowZoneObject(
                            id = it.groupId,
                            speedLimit = it.speedLimit,
                            startTime = it.mondayStart,
                            endTime = it.mondayEnd,
                            polygon = polygon,
                            markerPoint = markerPoint,
                        ))
                    }
                }
                Calendar.TUESDAY -> {
                    if (currentSecconds > it.tuesdayStart && currentSecconds < it.tuesdayEnd) {
                        result.add(SlowZoneObject(
                            id = it.groupId,
                            speedLimit = it.speedLimit,
                            startTime = it.tuesdayStart,
                            endTime = it.tuesdayEnd,
                            polygon = polygon,
                            markerPoint = markerPoint,
                        ))
                    }
                }
                Calendar.WEDNESDAY -> {
                    if (currentSecconds > it.wednesdayStart && currentSecconds < it.wednesdayEnd) {
                        result.add(SlowZoneObject(
                            id = it.groupId,
                            speedLimit = it.speedLimit,
                            startTime = it.wednesdayStart,
                            endTime = it.wednesdayEnd,
                            polygon = polygon,
                            markerPoint = markerPoint,
                        ))
                    }
                }
                Calendar.THURSDAY -> {
                    if (currentSecconds > it.thursdayStart && currentSecconds < it.thursdayEnd) {
                        result.add(SlowZoneObject(
                            id = it.groupId,
                            speedLimit = it.speedLimit,
                            startTime = it.thursdayStart,
                            endTime = it.thursdayEnd,
                            polygon = polygon,
                            markerPoint = markerPoint,
                        ))
                    }
                }
                Calendar.FRIDAY -> {
                    if (currentSecconds > it.fridayStart && currentSecconds < it.fridayEnd) {
                        result.add(SlowZoneObject(
                            id = it.groupId,
                            speedLimit = it.speedLimit,
                            startTime = it.fridayStart,
                            endTime = it.fridayEnd,
                            polygon = polygon,
                            markerPoint = markerPoint,
                        ))
                    }
                }
                Calendar.SATURDAY -> {
                    if (currentSecconds > it.saturdayStart && currentSecconds < it.saturdayEnd) {
                        result.add(SlowZoneObject(
                            id = it.groupId,
                            speedLimit = it.speedLimit,
                            startTime = it.saturdayStart,
                            endTime = it.saturdayEnd,
                            polygon = polygon,
                            markerPoint = markerPoint,
                        ))
                    }
                }
                Calendar.SUNDAY -> {
                    if (currentSecconds > it.sundayStart && currentSecconds < it.sundayEnd) {
                        result.add(SlowZoneObject(
                            id = it.groupId,
                            speedLimit = it.speedLimit,
                            startTime = it.sundayStart,
                            endTime = it.sundayEnd,
                            polygon = polygon,
                            markerPoint = markerPoint,
                        ))
                    }
                }
            }
        }
        return result
    }

/*
    private fun filterSlowZones(zones: List<SlowZone>) : List<SlowZone> {
        val calendar = Calendar.getInstance()
        val currentDay = calendar[Calendar.DAY_OF_WEEK]
        val currentSecconds = calendar.get(Calendar.SECOND)

        return zones.filter {
            when (currentDay) {
                Calendar.MONDAY -> currentSecconds > it.mondayStart && currentSecconds < it.mondayEnd
                Calendar.TUESDAY -> currentSecconds > it.tuesdayStart && currentSecconds < it.tuesdayEnd
                Calendar.WEDNESDAY -> currentSecconds > it.wednesdayStart && currentSecconds < it.wednesdayEnd
                Calendar.THURSDAY -> currentSecconds > it.thursdayStart && currentSecconds < it.thursdayEnd
                Calendar.FRIDAY -> currentSecconds > it.fridayStart && currentSecconds < it.fridayEnd
                Calendar.SATURDAY -> currentSecconds > it.saturdayStart && currentSecconds < it.saturdayEnd
                Calendar.SUNDAY -> currentSecconds > it.sundayStart && currentSecconds < it.sundayEnd
                else -> false
            }
        }
    }
*/

    companion object {
        private const val SHOW_CONTENT_ZOOM = 5f
        private const val CHECK_RENT_STATUS_DELAY = 3000L
    }
}

// old:
/*
    private val _mainUiState = MutableStateFlow(MainUiState())

    init {
        _mainUiState.value = MainUiState(isLogged = authManager.isLogged)
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.Login -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(isLogged = authManager.isLogged)
                }
            }
            is MainIntent.ChangeMapPosition -> {
                if (intent.zoom >= SHOW_CONTENT_ZOOM)
                    updateBikesAndParkings(intent.mapRect)
            }
            is MainIntent.BikesRendered -> {
                _mainUiState.update { currentState ->
                    currentState.copy(bikesUpdated = false)
                }
            }
            is MainIntent.ParkingsRendered -> {
                _mainUiState.update { currentState ->
                    currentState.copy(parkingsUpdated = false)
                }
            }
            // temp
            is MainIntent.Action1 -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(dialogState = true)
                }
            }
            is MainIntent.Action2 -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(bottomSheetState = true)
                }
            }
            is MainIntent.Action3 -> {
                _mainUiState.update { currentState ->
                    currentState.copy(bottomSheetState = true)
                }
            }
            is MainIntent.Dialog -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(dialogState = false)
                }
            }
            is MainIntent.BottomSheet -> {
                // ... some work here
                _mainUiState.update { currentState ->
                    currentState.copy(bottomSheetState = false)
                }
            }
        }
    }
                _mainUiState.update { currentState ->
                    currentState.copy(bikes = it, bikesUpdated = true)
                }

*/
