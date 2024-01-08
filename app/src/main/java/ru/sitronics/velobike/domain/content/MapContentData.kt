package ru.sitronics.velobike.domain.content

data class MapContentData(
    val bikes: List<Bike>? = null,
    val parkings: List<Parking>? = null,
    val slowZones: List<SlowZone>? = null,
)