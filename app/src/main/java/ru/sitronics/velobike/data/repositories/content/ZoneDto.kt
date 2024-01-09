package ru.sitronics.velobike.data.repositories.content

import ru.sitronics.velobike.data.repositories.ResponseDto
import ru.sitronics.velobike.domain.content.GeomGeo
import ru.sitronics.velobike.domain.content.MoveZone

data class MoveZonesDto(
    val pageMetadata: PageMetadata,
    val zones: List<ZoneDto>
) : ResponseDto<List<MoveZone>> {
    override fun toModel(): List<MoveZone> =
        zones.map { it.toModel() }
}

data class PageMetadata(
    val number: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int
)

data class ZoneDto(
    val additionalInfo: String,
    val childZones: List<Int>,
    val description: String,
    val geodata: GeoData,
    val id: Int,
    val name: String,
    val parentZone: Int,
    val type: Type
) : ResponseDto<MoveZone> {
    override fun toModel(): MoveZone =
        MoveZone(
            id = id,
            geomGeo = geodata.geojson,
        )
}

data class Type(
    val code: String,
    val id: Int,
    val name: String
)

data class GeoData(
    val center: Center,
    val geojson: GeomGeo,
    val id: Int
)

data class Center(
    val coordinates: List<Double>,
    val type: String
)