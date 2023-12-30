package ru.sitronics.velobike.presentation.main

import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.content.Bike
import ru.sitronics.velobike.domain.content.Parking

sealed class MainUiState {
    object Normal : MainUiState()
    object Login : MainUiState()
    data class BikesUpdated(val bikes: List<Bike>) : MainUiState()
    data class ParkingsUpdated(val parkings: List<Parking>) : MainUiState()
    data class ShowBikeDetail(val bike: Bike) : MainUiState()
    object ShowQrScan : MainUiState()
}

sealed class MainIntent {
    object Logged : MainIntent()
    data class ChangeMapPosition(val mapRect: MapRect, val zoom: Float) : MainIntent()
    data class TapMapObject(val userData: MarkerUserData?) : MainIntent()
    object QrScanClick : MainIntent()
    data class CloseBikeDetail(val startRide: Boolean = false) : MainIntent()
    data class CloseQrScan(val bikeNumber: String? = null) : MainIntent()
}
