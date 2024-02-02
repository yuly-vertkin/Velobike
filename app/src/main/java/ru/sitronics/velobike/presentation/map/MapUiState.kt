package ru.sitronics.velobike.presentation.map

import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.domain.rent.ActiveRent

sealed class MapUiState {
    object Normal : MapUiState()
    data class MapContent(val bikes: List<Bike>?, val stations: List<Parking>?, val parkings: List<Parking>?, val slowZones: List<SlowZoneObject>?, val showMarkers: Boolean) : MapUiState()
//    data class BikesUpdated(val bikes: List<Bike>) : MapUiState()
//    data class ParkingsUpdated(val stations: List<Parking>, val parkings: List<Parking>) : MapUiState()
//    data class ShowSlowZones(val slowZones: List<SlowZoneObject>, val showMarkers: Boolean) : MapUiState()
    data class MoveZones(val moveZone: MoveZoneObject) : MapUiState()
    data class BikeDetail(val bike: Bike, val fromQrScan: Boolean = false) : MapUiState()
    data class StationDetail(val station: Parking) : MapUiState()
    data class ParkingDetail(val parking: Parking) : MapUiState()
    data class QrScan(val show: Boolean, val fromBikeDetail: Boolean = false) : MapUiState()
    data class CurrentRent(val activeRent: ActiveRent?, val show: Boolean) : MapUiState()
    data class ActiveRentBar(val show: Boolean) : MapUiState()
    data class FinishingRent(val show: Boolean) : MapUiState()
    object ChooseParking : MapUiState()
    object WheelLock : MapUiState()
    object TakePhoto : MapUiState()
    data class FinishedRent(val activeRent: ActiveRent?) : MapUiState()
    data class Error(val title: String, val text: String) : MapUiState()
    data class Loading(val show: Boolean) : MapUiState()
}

sealed class MapIntent {
    object ResetState : MapIntent()
    object MapStart : MapIntent()
    object MapStop : MapIntent()
    data class ChangeMapPosition(val mapRect: MapRect, val zoom: Float) : MapIntent()
    data class MapObjectTap(val userData: MarkerUserData?) : MapIntent()
    object QrScanTap : MapIntent()
    data class CloseBikeDetail(val id: String?, val fromQrScan: Boolean, val latitude: Double?, val longitude: Double?) : MapIntent()
    data class CloseQrScan(val id: String? = null, val fromBikeDetail: Boolean, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class CloseActiveRent(val isClicked: Boolean = false, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    object ClickActiveRentBar : MapIntent()
    data class CloseFinishingRent(val isClicked: Boolean = false, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class CloseChooseParking(val isClicked: Boolean = false) : MapIntent()
    object CloseWheelLock : MapIntent()
    data class OnTakePhoto(val filePath: String? = null) : MapIntent()
    object CloseFinishedRent : MapIntent()
    object CloseError : MapIntent()
}
