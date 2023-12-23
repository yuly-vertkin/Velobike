package ru.sitronics.velobike.presentation.main

import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.content.Bike
import ru.sitronics.velobike.domain.content.Parking

sealed class MainUiState {
    object Normal : MainUiState()
    object Login : MainUiState()
    data class BikesUpdated(val bikes: List<Bike>) : MainUiState()
    data class ParkingsUpdated(val parkings: List<Parking>) : MainUiState()
}

sealed class MainIntent {
    object Logged : MainIntent()
    data class ChangeMapPosition(val mapRect: MapRect, val zoom: Float) : MainIntent()
}

// old:
/*
data class MainUiState(
    val isLogged: Boolean = false,
    val bikes: List<Bike> = emptyList(),
    val parkings: List<Parking> = emptyList(),
    val bikesUpdated: Boolean = false,
    val parkingsUpdated: Boolean = false,
    // temp
    val dialogState: Boolean = false,
    val bottomSheetState: Boolean = false,
)
//    val bikes: List<Bike> = listOf(Bike("1", "2", 0, MOSCOW_LAT, MOSCOW_LON, null, BikeInventoryStatus.IN_CITY, BikeOperativeStatus.STATIONED)),

sealed class MainIntent {
    object Logged : MainIntent()
    data class ChangeMapPosition(val mapRect: MapRect, val zoom: Float) : MainIntent()
    object BikesRendered : MainIntent()
    object ParkingsRendered : MainIntent()
    // temp
    data class Action1(val someData: Boolean) : MainIntent()
    data class Action2(val someData: Boolean) : MainIntent()
    object Action3 : MainIntent()
    data class Dialog(val someData: Boolean) : MainIntent()
    object BottomSheet : MainIntent()
}
*/
