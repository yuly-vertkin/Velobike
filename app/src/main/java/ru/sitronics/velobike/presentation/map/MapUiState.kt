package ru.sitronics.velobike.presentation.map

import android.content.Context
import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.domain.rent.Rent

sealed class MapUiState {
    data object Normal : MapUiState()
//    data class MapContent(val bikes: List<Bike>?, val stations: List<Parking>?, val parkings: List<Parking>?, val slowZones: List<SlowZoneObject>?, val showMarkers: Boolean) : MapUiState()
    data class Bikes(val bikes: List<Bike>) : MapUiState()
    data class Parkings(val stations: List<Parking>, val parkings: List<Parking>) : MapUiState()
    data class SlowZones(val slowZones: List<SlowZoneObject>, val showMarkers: Boolean) : MapUiState()
    data class MoveZones(val moveZone: MoveZoneObject) : MapUiState()
    data class BikeDetail(val bike: Bike, val fromQrScan: Boolean, val enableAction: Boolean) : MapUiState()
    data class StationDetail(val station: Parking) : MapUiState()
    data class ParkingDetail(val parking: Parking) : MapUiState()
    data object CloseAllDetails : MapUiState()
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
    data class ChatTap(val context: Context) : MapIntent()
    data class Search(val searchStr: String, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class SearchAction(val id: String?) : MapIntent()
    data object ScanQrTap : MapIntent()
    data class ScanQrAction(val action: DialogAction, val id: String? = null, val fromBikeDetail: Boolean, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class BikeDetailAction(val action: DialogAction, val id: String?, val fromQrScan: Boolean, val latitude: Double?, val longitude: Double?) : MapIntent()
    data class ActiveRentAction(val action: DialogAction, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data object ClickActiveRentBar : MapIntent()
    data class FinishingRentAction(val action: DialogAction) : MapIntent()
    data class ChooseParkingAction(val action: DialogAction) : MapIntent()
    data object WheelLockAction : MapIntent()
    data class TakePhotoAction(val action: DialogAction, val filePath: String) : MapIntent()
    data class FinishedRentAction(val action: DialogAction, val rent: Rent?, val rating: Int?) : MapIntent()
    data object ErrorAction : MapIntent()
}

enum class DialogAction {
    CLICK, DISMISS, BACK
}

enum class RentDialogState {
    NONE, ACTIVE_RENT_BAR, WHEEL_LOCK, CHOOSE_PARKING, SEARCH;

    fun isNone() = this == NONE
}