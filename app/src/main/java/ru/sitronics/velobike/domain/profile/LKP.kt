package ru.sitronics.velobike.domain.profile

import com.google.gson.annotations.SerializedName

data class LKPStatusData(
    val status: LKPStatus
)

enum class LKPStatus {
    @SerializedName("FULL")
    FULL,
    @SerializedName("HAS_KEY")
    HAS_KEY,
    @SerializedName("NEW")
    NEW,
    @SerializedName("CANCELED")
    CANCELED,
    NONE
}

data class MetroPasswordParameters(
    @SerializedName("attempt_count")
    val attemptCount: Int,
    @SerializedName("expires_in")
    val expiresIn: Int,
    val length: Int
)