package ru.sitronics.velobike.data.repositories.map

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.sitronics.velobike.data.repositories.ResponseDto
import ru.sitronics.velobike.domain.map.Bike
import ru.sitronics.velobike.domain.map.BikeInventoryStatus
import ru.sitronics.velobike.domain.map.BikeOperativeStatus

@Parcelize
data class BikeParams(
    @SerializedName("inventoryStatus")
    val inventoryStatuses: List<String>,
    @SerializedName("operativeStatuses")
    val operativeStatuses: List<String>,
    @SerializedName("boundingBox")
    val boundingBox: BoundingBox,
) : Parcelable

@Parcelize
data class BikesDto(
    @SerializedName("result")
    val bikes: List<BikeDto>?,
) : Parcelable, ResponseDto<List<Bike>> {
    override fun toModel(): List<Bike> =
        bikes?.map { it.toModel() } ?: emptyList()
}

@Parcelize
data class BikeDto(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("frameNumber")
    val frameNumber: String?,
    @SerializedName("vehicleType")
    val vehicleType: VehicleType?,
    @SerializedName("operativeStatusChangeDate")
    val operativeStatusChangeDate: Long?,
    @SerializedName("inventoryStatusChangeDate")
    val inventoryStatusChangeDate: Long?,
    @SerializedName("operativeStatus")
    val operativeStatus: BikeOperativeStatus?,
    @SerializedName("inventoryStatus")
    val inventoryStatus: BikeInventoryStatus?,
    @SerializedName("installedDevSerialNum")
    val serialNum: String?,
    @SerializedName("telemetry")
    val telemetry: Telemetry?,
    @SerializedName("maintenance")
    val maintenance: Maintenance?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("parking")
    val parking: Parking?,
    @SerializedName("isBlockingTag")
    val isBlockingTag: Boolean?,
) : Parcelable, ResponseDto<Bike> {
    override fun toModel(): Bike =
        Bike(
            id = frameNumber ?: "",
            deviceId = serialNum ?: "",
            batteryPower = telemetry?.batteryLevel ?: 0,
            latitude = telemetry?.coordinates?.latitude ?: 0.0,
            longitude = telemetry?.coordinates?.longitude ?: 0.0,
            vehicleInventoryStatus = inventoryStatus ?: BikeInventoryStatus.UNKNOWN,
            vehicleOperativeStatus = operativeStatus ?: BikeOperativeStatus.UNKNOWN,
        )
}

@Parcelize
data class BoundingBox(
    @SerializedName("swCorner")
    val swCorner: Coordinates,
    @SerializedName("neCorner")
    val neCorner: Coordinates,
) : Parcelable

@Parcelize
data class Coordinates(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
) : Parcelable

@Parcelize
data class VehicleType(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("vehicleTypeName")
    val typeName: String?,
    @SerializedName("transportType")
    val transportType: String?,
    @SerializedName("driveType")
    val driveType: String?,
    @SerializedName("batteryType")
    val batteryType: String?,
    @SerializedName("description")
    val description: String?,
) : Parcelable

@Parcelize
data class Telemetry(
    @SerializedName("networkStatus")
    val networkStatus: String?,
    @SerializedName("batteryLevel")
    val batteryLevel: Int?,
    @SerializedName("offlineDetectTS")
    val offlineDetectTS: Long?,
    @SerializedName("lastChangeTS")
    val lastChangeTS: Long?,
    @SerializedName("coordinates")
    val coordinates: Coordinates?,
    @SerializedName("coordinatesChangeTS")
    val coordinatesChangeTS: Long?,
    @SerializedName("positionStatus")
    val positionStatus: String?,
    @SerializedName("currentSpeed")
    val currentSpeed: Double?,
    @SerializedName("locked")
    val locked: Boolean?,
    @SerializedName("location")
    val location: List<Location>?,
    @SerializedName("district")
    val district: String?,
) : Parcelable

@Parcelize
data class Location(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("speedLimit")
    val speedLimit: Int?,
) : Parcelable

@Parcelize
data class Maintenance(
    @SerializedName("lastMaintenanceDatetime")
    val lastMaintenanceDatetime: Long?,
    @SerializedName("totalMaintenanceTime")
    val totalMaintenanceTime: Long?,
    @SerializedName("timeInMaintenance")
    val timeInMaintenance: Long?,
    @SerializedName("totalRentTime")
    val totalRentTime: Long?,
    @SerializedName("rentTimeAfterLastMaintenance")
    val rentTimeAfterLastMaintenance: Long?,
) : Parcelable

@Parcelize
data class Parking(
    @SerializedName("parkingId")
    val parkingId: String?,
    @SerializedName("externalParkingId")
    val externalParkingId: String?,
) : Parcelable
