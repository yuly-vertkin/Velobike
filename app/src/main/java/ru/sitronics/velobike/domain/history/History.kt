package ru.sitronics.velobike.domain.history

import com.google.gson.annotations.SerializedName
import java.util.Date

const val HISTORY_PAGINATION_SIZE = 15

data class HistoryData (
    val someData: Boolean = false
)

data class HistoryParams(
    val limit: Int,
    val offset: Int,
    val type: HistoryType? = null
)

enum class HistoryType(val value: String) {
    RIDE("Ride"),
    PAY("Pay")
}

data class HistoryItemPackage(
    @SerializedName("Items")
    val items : List<HistoryItem>,
    @SerializedName("HasMore")
    val hasMore : Boolean,
    @SerializedName("TotalCalories")
    val totalCalories : Double?,
    @SerializedName("TotalDistance")
    val totalDistance : Double?,
    @SerializedName("TotalRides")
    val totalRides : Int,
    @SerializedName("TotalRidesTime")
    val totalRidesTime : String?,
)/* : ResponseDto<List<HistoryItem>> {
    override fun toModel(): List<HistoryItem> = items
}*/

data class HistoryItem(
    @SerializedName("Id")
    val id: String,
    @SerializedName("StartDate")
    val startDate: Date,
    @SerializedName("Duration")
    val duration: Date?,
    @SerializedName("Type")
    val type: HistoryItemType,
    @SerializedName("Rejected")
    val rejected: Boolean = false,
    @SerializedName("BikeType")
    val bikeTypes: String,
    @SerializedName("BikeId")
    val bikeId: String?,
    @SerializedName("Calories")
    val calories: Double,
    @SerializedName("StartBikeParkingNumber")
    val startBikeParkingNumber: String?,
    @SerializedName("EndBikeParkingNumber")
    val endBikeParkingNumber: String?,
    @SerializedName("CoveredDistance")
    val coveredDistance: Double = 0.0,
    @SerializedName("StartBikeParkingAddress")
    val startBikeParkingAddress: String?,
    @SerializedName("EndBikeParkingAddress")
    val endBikeParkingAddress: String?,
    @SerializedName("StartBikeParkingName")
    val startBikeParkingName: String?,
    @SerializedName("EndBikeParkingName")
    val endBikeParkingName: String?,
    @SerializedName("StartBikeSlotNumber")
    val startBikeSlotNumber: String?,
    @SerializedName("EndBikeSlotNumber")
    val endBikeSlotNumber: String?,
    @SerializedName("Contract")
    val contract: String?,
    @SerializedName("PanMask")
    val panMask: String?,
    @SerializedName("timestamp")
    val timestamp: Long = 0,
    @SerializedName("RentalStartId")
    val rentalStartId: String,
    @SerializedName("Price")
    val price: Double?,
    @SerializedName("BookingDate")
    val bookingDate: String?,
    @SerializedName("BookingId")
    val bookingId: String?,
    @SerializedName("StationId")
    val stationId: String?,
    @SerializedName("Cost")
    val cost: Double?,
    @SerializedName("Address")
    val address: String?,
//    @SerializedName("TrackData")
//    val trackData: TrackData,
    @SerializedName("TariffName")
    val tariffName: String?,
    @SerializedName("TariffCost")
    val tariffCost: Double = 0.0,
//    @SerializedName("Assessment")
//    val assessment: Assessment? = null,
    @SerializedName("BoardingCost")
    val boardingCost: Double = 0.0,
    @SerializedName("TimeCost")
    val timeCost: Double = 0.0,
    @SerializedName("BonusWriteoffAmount")
    val bonusWriteoffAmount: Int = 0
)

enum class HistoryItemType(val i: Int) {
    Unknown(-1),
    Deposit(0),
    Ride(1),
    Pay(2),
    DepositWithdraw(3),
    Current(4),
    Booking(5),
}
