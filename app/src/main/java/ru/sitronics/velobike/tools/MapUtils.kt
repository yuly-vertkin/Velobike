package ru.sitronics.velobike.tools

import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import ru.sitronics.velobike.domain.map.MoveZone
import ru.sitronics.velobike.domain.map.SlowZone
import ru.sitronics.velobike.presentation.map.MoveZoneObject
import ru.sitronics.velobike.presentation.map.SlowZoneObject
import java.util.Calendar

fun filterSlowZones(zones: List<SlowZone>?) : List<SlowZoneObject> {
    val calendar = Calendar.getInstance()
    val currentDay = calendar[Calendar.DAY_OF_WEEK]
    val currentSecconds = calendar.get(Calendar.SECOND)
    val result = mutableListOf<SlowZoneObject>()

    zones?.forEach {
        val polygon = coordinatesToPolygon(it.geomGeo.coordinates)
        val markerPoint = getSlowZoneMarkerPoint(polygon.outerRing.points)
        when (currentDay) {
            Calendar.MONDAY -> {
                if (currentSecconds > it.mondayStart && currentSecconds < it.mondayEnd) {
                    result.add(
                        SlowZoneObject(
                        id = it.groupId,
                        speedLimit = it.speedLimit,
                        startTime = it.mondayStart,
                        endTime = it.mondayEnd,
                        polygon = polygon,
                        markerPoint = markerPoint,
                    )
                    )
                }
            }
            Calendar.TUESDAY -> {
                if (currentSecconds > it.tuesdayStart && currentSecconds < it.tuesdayEnd) {
                    result.add(
                        SlowZoneObject(
                        id = it.groupId,
                        speedLimit = it.speedLimit,
                        startTime = it.tuesdayStart,
                        endTime = it.tuesdayEnd,
                        polygon = polygon,
                        markerPoint = markerPoint,
                    )
                    )
                }
            }
            Calendar.WEDNESDAY -> {
                if (currentSecconds > it.wednesdayStart && currentSecconds < it.wednesdayEnd) {
                    result.add(
                        SlowZoneObject(
                        id = it.groupId,
                        speedLimit = it.speedLimit,
                        startTime = it.wednesdayStart,
                        endTime = it.wednesdayEnd,
                        polygon = polygon,
                        markerPoint = markerPoint,
                    )
                    )
                }
            }
            Calendar.THURSDAY -> {
                if (currentSecconds > it.thursdayStart && currentSecconds < it.thursdayEnd) {
                    result.add(
                        SlowZoneObject(
                        id = it.groupId,
                        speedLimit = it.speedLimit,
                        startTime = it.thursdayStart,
                        endTime = it.thursdayEnd,
                        polygon = polygon,
                        markerPoint = markerPoint,
                    )
                    )
                }
            }
            Calendar.FRIDAY -> {
                if (currentSecconds > it.fridayStart && currentSecconds < it.fridayEnd) {
                    result.add(
                        SlowZoneObject(
                        id = it.groupId,
                        speedLimit = it.speedLimit,
                        startTime = it.fridayStart,
                        endTime = it.fridayEnd,
                        polygon = polygon,
                        markerPoint = markerPoint,
                    )
                    )
                }
            }
            Calendar.SATURDAY -> {
                if (currentSecconds > it.saturdayStart && currentSecconds < it.saturdayEnd) {
                    result.add(
                        SlowZoneObject(
                        id = it.groupId,
                        speedLimit = it.speedLimit,
                        startTime = it.saturdayStart,
                        endTime = it.saturdayEnd,
                        polygon = polygon,
                        markerPoint = markerPoint,
                    )
                    )
                }
            }
            Calendar.SUNDAY -> {
                if (currentSecconds > it.sundayStart && currentSecconds < it.sundayEnd) {
                    result.add(
                        SlowZoneObject(
                        id = it.groupId,
                        speedLimit = it.speedLimit,
                        startTime = it.sundayStart,
                        endTime = it.sundayEnd,
                        polygon = polygon,
                        markerPoint = markerPoint,
                    )
                    )
                }
            }
        }
    }
    return result
}

private fun coordinatesToPolygon(coordinates: List<List<List<Double>>>) : Polygon {
    var outerRing = LinearRing()
    val innerRings = mutableListOf<LinearRing>()

    coordinates.forEachIndexed { index, figure ->
        val points = mutableListOf<Point>()
        figure.forEach {
            points.add(Point(it.last(), it.first()))
        }
        if (index == 0) outerRing = LinearRing(points)
        else innerRings.add(LinearRing(points))
    }
    return Polygon(outerRing, innerRings)
}

private fun getSlowZoneMarkerPoint(points: List<Point>) : Point {
    val latitude = points.sumOf { it.latitude }
    val longitude = points.sumOf { it.longitude }
    return Point(latitude / points.size, longitude / points.size)
}

fun filterMoveZones(zones: List<MoveZone>) : MoveZoneObject {
    val outerPoints = listOf(
        Point(89.0, -180.0),
        Point(-89.0, -180.0),
        Point(-89.0, 180.0),
        Point(89.0, 180.0),
    )
    val innerRings = mutableListOf<LinearRing>()

    zones.forEach {
        val polygons = it.geomGeo.coordinates

        polygons.forEach { polygon ->
            val innerPoints = mutableListOf<Point>()
            polygon.forEach { coord ->
                innerPoints.add(Point(coord.last(), coord.first()))
            }
            innerRings.add(LinearRing(innerPoints))
        }
    }

    return MoveZoneObject(
        id = zones.firstOrNull()?.id ?: 0,
        polygon = Polygon(LinearRing(outerPoints), innerRings),
    )
}
