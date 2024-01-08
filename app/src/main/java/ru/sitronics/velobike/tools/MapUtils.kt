package ru.sitronics.velobike.tools

import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon

fun coordinatesToPolygon(coordinates: List<List<List<Double>>>) : Polygon {
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

fun getSlowZoneMarkerPoint(points: List<Point>) : Point {
    val latitude = points.sumOf { it.latitude }
    val longitude = points.sumOf { it.longitude }
    return Point(latitude / points.size, longitude / points.size)
}
