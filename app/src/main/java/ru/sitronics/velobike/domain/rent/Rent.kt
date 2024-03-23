package ru.sitronics.velobike.domain.rent

import android.os.Parcelable
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.map.Bike

data class RentData(
    val rentBike: Bike? = null,
)

data class StartRentParams(
    val frameNumber: String,
    val isUsedQr: Boolean,
    val clientGeoPosition: ClientGeoPosition,
)

data class FinishRentParams(
    val id: String,
    val frameNumber: String,
    val deviceId: String,
    val clientGeoPosition: ClientGeoPosition,
)

data class ClientGeoPosition(
    val lat: Double,
    val lon: Double
)

data class Rent(
    val rentId: String,
    val externalClientId: String? = null,
    val frameNumber: String,
    val startParkingId: String,
    val rentStatus: MainRentStatus? = null,
    val failedReason: FailedReason? = null,
    val startTime: Long,
    val updateTime: Long? = null,
    val startPortNumber: Int,
    val deviceId: String,
    var bike: Bike? = null,
    var cost: Int = 0,
    var isOld: Boolean = false
)

@Parcelize
data class RentStatus(
    val id: String,
    val frameNumber: String? = null,
    val deviceId: String? = null,
    val status: MainRentStatus? = null,
    val processStatus: ProgressStatus? = null,
    val failedReason: FailedReason? = null,
) : Parcelable

data class FinishedRentOld(
    @SerializedName("rental_id")
    val rentId : String,
    @SerializedName("bike_id")
    val bikeId : String,
)

enum class MainRentStatus {
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,
    @SerializedName("ERROR_START")
    ERROR_START,
    @SerializedName("CHECK_START")
    CHECK_START,
    @SerializedName("CHECK_END")
    CHECK_END,
    @SerializedName("WAIT_TO_RETURN_IN_PROGRESS")
    WAIT_TO_RETURN_IN_PROGRESS,
    @SerializedName("DONE")
    DONE,
    @SerializedName("TECH_DONE")
    TECH_DONE;

    fun isDone() : Boolean =
        this == DONE || this == TECH_DONE
}

enum class ProgressStatus {
    @SerializedName("L5_OBTAIN_CHAIN_LOCK")
    L5_OBTAIN_CHAIN_LOCK,
    @SerializedName("L5_UNLOCK_CHAIN")
    L5_UNLOCK_CHAIN,
    @SerializedName("WAIT_CLOSE_CHAIN_LOCK")
    WAIT_CLOSE_CHAIN_LOCK,
    @SerializedName("WAIT_POSITION_FROM_CLIENT")
    WAIT_POSITION_FROM_CLIENT,
    @SerializedName("L0_RESET_LOCK")
    L0_RESET_LOCK,
    @SerializedName("D0_GET_POSITIONING")
    D0_GET_POSITIONING,
    @SerializedName("S5_OBTAIN_LOCK_INFO")
    S5_OBTAIN_LOCK_INFO,
    @SerializedName("WAIT_CLOSE_LOCK")
    WAIT_CLOSE_LOCK,
    @SerializedName("L5_LOCK_CHAIN")
    L5_LOCK_CHAIN,
    @SerializedName("S6_OBTAIN_SINGLE_RIDING")
    S6_OBTAIN_SINGLE_RIDING,
    @SerializedName("WAIT_UPLOAD_PHOTO")
    WAIT_UPLOAD_PHOTO,
    @SerializedName("WAIT_PARKING_FROM_CLIENT")
    WAIT_PARKING_FROM_CLIENT,
}

enum class FailedReason(@StringRes val messageIdStart: Int, @StringRes val messageIdFinish: Int) {
    @SerializedName("ACCOUNT_BLOCKED")
    ACCOUNT_BLOCKED(R.string.start_omni_failed_account_blocked, R.string.finish_omni_failed_default),
    @SerializedName("ACCOUNT_DEBT")
    ACCOUNT_DEBT(R.string.start_omni_failed_account_debt, R.string.finish_omni_failed_default),
    @SerializedName("ACCOUNT_HAS_RENT_ALREADY")
    ACCOUNT_HAS_RENT_ALREADY(R.string.start_omni_failed_has_rent_already, R.string.finish_omni_failed_default),
    @SerializedName("BIKE_NOT_ALLOWED")
    BIKE_NOT_ALLOWED(R.string.start_omni_failed_bike_not_allowed, R.string.finish_omni_failed_default),
    @SerializedName("BIKE_IS_OFFLINE")
    BIKE_IS_OFFLINE(R.string.start_omni_failed_bike_is_offline, R.string.finish_omni_failed_default),
    @SerializedName("OPEN_CHAIN_ERROR")
    OPEN_CHAIN_ERROR(R.string.start_omni_failed_open_chain_error, R.string.finish_omni_failed_open_chain_error),
    @SerializedName("OPEN_LOCK_ERROR")
    OPEN_LOCK_ERROR(R.string.start_omni_failed_open_lock_error, R.string.finish_omni_failed_open_lock_error),
    @SerializedName("PARKING_NOT_ALLOWED")
    PARKING_NOT_ALLOWED(R.string.start_omni_failed_default, R.string.finish_omni_failed_parking_not_allowed),
    @SerializedName("NEED_CLOSE_CHAIN_LOCK")
    NEED_CLOSE_CHAIN_LOCK(R.string.need_close_chain_error, R.string.start_omni_failed_default),
    @SerializedName("NEED_CLOSE_LOCK")
    NEED_CLOSE_LOCK(R.string.need_close_error, R.string.start_omni_failed_default),
    @SerializedName("USER_CANCEL_START")
    USER_CANCEL_START(R.string.need_close_error, R.string.start_omni_failed_default),
    @SerializedName("BIKE_IS_FAR_FROM_USER")
    BIKE_IS_FAR_FROM_USER(R.string.far_from_user_error, R.string.start_omni_failed_default),
}

data class ChooseParkingParams(
    val externalParkingId: String
)

data class Feedback(
    val comments: String,
    val customerExternalId: String,
    val rate: String,
    val rentId: String,
    val vehicleFrameNumber: String,
    val vehicleType: String,
    val frontWheel: Boolean = false,
    val handlebar: Boolean = false,
    val backWheel: Boolean = false,
    val chain: Boolean = false,
    val chainLock: Boolean = false,
    val pedal: Boolean = false,
    val saddle: Boolean = false,
    val tireLock: Boolean = false,
)

data class FeedbackRes(
    val id: Int,
)