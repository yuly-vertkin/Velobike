package ru.sitronics.velobike.domain.map

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Bike(
    val id: String,
    val deviceId: String,
    val batteryPower: Int,
    val latitude: Double,
    val longitude: Double,
    val currnetRentId: UUID? = null,
    val vehicleInventoryStatus: BikeInventoryStatus,
    val vehicleOperativeStatus: BikeOperativeStatus,
)

enum class BikeOperativeStatus(val value: String) {
    @SerializedName("UNKNOWN")
    UNKNOWN("UNKNOWN"),
    @SerializedName("STATIONED")
    STATIONED("STATIONED");
}

enum class BikeInventoryStatus(val value: String) {
    @SerializedName("UNKNOWN")
    UNKNOWN("UNKNOWN"),
    @SerializedName("IN_CITY")
    IN_CITY("IN_CITY");
}
