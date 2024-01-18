package ru.sitronics.velobike.domain.map

data class MapContentData(
    val bikes: List<Bike>? = null,
    val stations: List<Parking>? = null,
    val parkings: List<Parking>? = null,
    val slowZones: List<SlowZone>? = null,
    val moveZones: List<MoveZone>? = null,
)