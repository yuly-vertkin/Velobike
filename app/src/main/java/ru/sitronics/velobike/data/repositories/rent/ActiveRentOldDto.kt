package ru.sitronics.velobike.data.repositories.rent

import com.google.gson.annotations.SerializedName
import ru.sitronics.velobike.data.repositories.ResponseDto
import ru.sitronics.velobike.domain.rent.ActiveRent
import ru.sitronics.velobike.domain.rent.MainRentStatus

data class ActiveRentOldDto(
    @SerializedName("rental_start_id")
    val rentalStartId : String,
    @SerializedName("bike_id")
    val bikeId : String,
    @SerializedName("bike_type")
    val bikeType : Int,
    @SerializedName("ride_started_at")
    val startDate : Long,
    @SerializedName("departure_station_id")
    val startBikeParkingNumber : String,
    @SerializedName("remaining_free_time_secs")
    val remainFreeTime : Int,
    @SerializedName("spent_free_time_secs")
    val spentFreeTime: Int,
) : ResponseDto<ActiveRent> {
    override fun toModel(): ActiveRent =
        ActiveRent(
            rentId = rentalStartId,
            frameNumber = bikeId,
            startParkingId = startBikeParkingNumber,
            rentStatus = MainRentStatus.IN_PROGRESS,
            failedReason = null,
            startTime = startDate * 1000, // old rent time in sec.
            updateTime = null,
            startPortNumber = 0,
            deviceId = "",
        )
}