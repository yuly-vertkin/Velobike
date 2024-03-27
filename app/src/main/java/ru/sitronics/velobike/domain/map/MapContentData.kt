package ru.sitronics.velobike.domain.map

data class MapContentData(
    var bikes: List<Bike>? = null,
    var stations: List<Parking>? = null,
    var parkings: List<Parking>? = null,
    var slowZones: List<SlowZone>? = null,
    var moveZones: List<MoveZone>? = null,
)

data class MapContent(
    var bikes: List<Bike>? = null,
    var stations: List<Parking>? = null,
    var parkings: List<Parking>? = null,
    var slowZones: List<SlowZone>? = null,
    var showMarkers: Boolean = false,
)