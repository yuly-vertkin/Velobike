package ru.sitronics.velobike.presentation.map

import android.content.Context
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.domain.rent.Rent

sealed class MapUiState {
    data object Normal : MapUiState()
    data class MapContent(val bikes: List<Bike>?, val stations: List<Parking>?, val parkings: List<Parking>?, val slowZones: List<SlowZoneObject>?, val showMarkers: Boolean) : MapUiState()
//    data class BikesUpdated(val bikes: List<Bike>) : MapUiState()
//    data class ParkingsUpdated(val stations: List<Parking>, val parkings: List<Parking>) : MapUiState()
//    data class ShowSlowZones(val slowZones: List<SlowZoneObject>, val showMarkers: Boolean) : MapUiState()
    data class MoveZones(val moveZone: MoveZoneObject) : MapUiState()
    data class BikeDetail(val bike: Bike, val fromQrScan: Boolean = false) : MapUiState()
    data class StationDetail(val station: Parking) : MapUiState()
    data class ParkingDetail(val parking: Parking) : MapUiState()
    data class QrScan(val show: Boolean, val fromBikeDetail: Boolean = false) : MapUiState()
    data class Search(val parkings: List<Parking>?) : MapUiState()
    data class ActiveRent(val rent: Rent?, val show: Boolean) : MapUiState()
    data class ActiveRentBar(val show: Boolean) : MapUiState()
    data class FinishingRent(val show: Boolean) : MapUiState()
    data object ChooseParking : MapUiState()
    data object WheelLock : MapUiState()
    data object TakePhoto : MapUiState()
    data class FinishedRent(val rent: Rent?) : MapUiState()
    data class ChatUnreadMessages(val count: Int) : MapUiState()
    data class Error(val title: String, val text: String) : MapUiState()
    data class Loading(val show: Boolean) : MapUiState()
    data class QrScanButton(val show: Boolean) : MapUiState()
}

sealed class MapIntent {
    data object ResetState : MapIntent()
    data object MapStart : MapIntent()
    data object MapStop : MapIntent()
    data class ChangeMapPosition(val mapRect: MapRect, val zoom: Float) : MapIntent()
    data class MapObjectTap(val userData: MarkerUserData?) : MapIntent()
    data object QrScanTap : MapIntent()
    data class ChatTap(val context: Context) : MapIntent()
    data class Search(val searchStr: String, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class SearchAction(val id: String?) : MapIntent()
    data class BikeDetailAction(val id: String?, val fromQrScan: Boolean, val latitude: Double?, val longitude: Double?) : MapIntent()
    data class QrScanAction(val id: String? = null, val fromBikeDetail: Boolean, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class ActiveRentAction(val isClicked: Boolean = false, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data object ClickActiveRentBar : MapIntent()
    data class FinishingRentAction(val isClicked: Boolean = false, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class ChooseParkingAction(val isClicked: Boolean = false) : MapIntent()
    data object WheelLockAction : MapIntent()
    data class OnTakePhoto(val filePath: String? = null) : MapIntent()
    data class FinishedRentAction(val rent: Rent?, val rating: Int) : MapIntent()
    data object ErrorAction : MapIntent()
}

enum class MapDialogState {
    NONE, ACTIVE_RENT_BAR, WHEEL_LOCK, CHOOSE_PARKING, SEARCH;

    fun isNone() = this == NONE
}