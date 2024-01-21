package ru.sitronics.velobike.presentation.map

import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon

const val MOSCOW_LAT = 55.75222
const val MOSCOW_LON = 37.61556
const val TEXT_SIZE = 14

data class SlowZoneObject(
    val id: Int,
    val speedLimit: Int,
    val startTime: Int,
    val endTime: Int,
    val polygon: Polygon,
    val markerPoint: Point,
)

data class MoveZoneObject(
    val id: Int,
    val polygon: Polygon,
)

sealed class MarkerUserData {
    data class Bike(val id: String) : MarkerUserData()
    data class Station(val id: String) : MarkerUserData()
    data class Parking(val id: String) : MarkerUserData()
    data class SlowZone(val id: Int) : MarkerUserData()
    data class MoveZone(val id: Int) : MarkerUserData()
}

enum class ActiveRentState {
    SHOW, DISMISS, CLICK
}