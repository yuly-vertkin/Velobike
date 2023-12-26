package ru.sitronics.velobike.presentation.main

const val MOSCOW_LAT = 55.75222
const val MOSCOW_LON = 37.61556

data class Marker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val userData: MarkerUserData,
)

sealed class MarkerUserData {
    data class Bike(val id: String) : MarkerUserData()
    data class Parking(val id: String) : MarkerUserData()
    data class SlowZone(val id: String) : MarkerUserData()
    data class MoveZone(val id: String) : MarkerUserData()
}