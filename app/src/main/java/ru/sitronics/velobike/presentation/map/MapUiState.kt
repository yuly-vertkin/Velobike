package ru.sitronics.velobike.presentation.map

import ru.sitronics.velobike.domain.MapRect
import ru.sitronics.velobike.domain.content.Bike

sealed class MapUiState {
    object Normal : MapUiState()
    data class BikesUpdated(val bikes: List<Marker>) : MapUiState()
    data class ParkingsUpdated(val stations: List<Marker>, val parkings: List<Marker>) : MapUiState()
    data class ShowSlowZones(val slowZones: List<SlowZoneObject>, val showMarkers: Boolean) : MapUiState()
    data class ShowMoveZones(val moveZone: MoveZoneObject) : MapUiState()
    data class ShowBikeDetail(val bike: Bike) : MapUiState()
    object ShowQrScan : MapUiState()
    data class ShowError(val error: String): MapUiState()
}

sealed class MapIntent {
    object MapStart : MapIntent()
    data class ChangeMapPosition(val mapRect: MapRect, val zoom: Float) : MapIntent()
    data class MapObjectTap(val userData: MarkerUserData?) : MapIntent()
    object QrScanTap : MapIntent()
    data class CloseBikeDetail(val bikeId: String? = null, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    data class CloseQrScan(val bikeId: String? = null, val latitude: Double? = null, val longitude: Double? = null) : MapIntent()
    object CloseError : MapIntent()
}
