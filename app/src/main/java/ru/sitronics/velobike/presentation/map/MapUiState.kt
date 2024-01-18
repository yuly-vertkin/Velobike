package ru.sitronics.velobike.presentation.map

import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.Parking
import ru.sitronics.velobike.domain.rent.ActiveRent

sealed class MapUiState {
    object Normal : MapUiState()
    data class BikesUpdated(val bikes: List<Bike>) : MapUiState()
    data class ParkingsUpdated(val stations: List<Parking>, val parkings: List<Parking>) : MapUiState()
    data class ShowSlowZones(val slowZones: List<SlowZoneObject>, val showMarkers: Boolean) : MapUiState()
    data class ShowMoveZones(val moveZone: MoveZoneObject) : MapUiState()
    data class ShowBikeDetail(val bike: Bike) : MapUiState()
    data class ShowStationDetail(val station: Parking) : MapUiState()
    data class ShowParkingDetail(val parking: Parking) : MapUiState()
    object ShowQrScan : MapUiState()
    data class ShowError(val error: String): MapUiState()
    data class ShowActiveRent(val activeRent: ActiveRent? = null) : MapUiState()
}

sealed class MapIntent {
    object MapStart : MapIntent()
    object MapStop : MapIntent()
    data class ChangeMapPosition(val mapRect: MapRect, val zoom: Float) : MapIntent()
    data class MapObjectTap(val userData: MarkerUserData?) : MapIntent()
    object QrScanTap : MapIntent()
    data class CloseBikeDetail(val id: String? = null, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class CloseQrScan(val id: String? = null, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class CloseParkingDetail(val id: String? = null) : MapIntent()
    object CloseError : MapIntent()
    data class ActiveRentAction(val finishRent: Boolean, val isClosed: Boolean, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
}
