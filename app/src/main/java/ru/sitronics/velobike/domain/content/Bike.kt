package ru.sitronics.velobike.domain.content

import java.util.UUID

data class Bike(
    val deviceId: String,
    val bikeSerialNumber: String,
    val batteryPower: Int,
    val latitude: Double,
    val longitude: Double,
    val currnetRentId: UUID? = null,
    val vehicleInventoryStatus: BikeInventoryStatus,
    val vehicleOperativeStatus: BikeOperativeStatus,
)

enum class BikeOperativeStatus(val value: String) {
    UNKNOWN("UNKNOWN"),
    STATIONED("STATIONED");

    companion object {
        fun fromString(string: String?): BikeOperativeStatus {
            return BikeOperativeStatus.values().firstOrNull { it.value == string } ?: UNKNOWN
        }
    }
}

enum class BikeInventoryStatus(val value: String) {
    UNKNOWN("UNKNOWN"),
    IN_CITY("IN_CITY");

    companion object {
        fun fromString(string: String?): BikeInventoryStatus {
            return BikeInventoryStatus.values().firstOrNull { it.value == string } ?: UNKNOWN
        }
    }
}
