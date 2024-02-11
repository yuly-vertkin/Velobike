package ru.sitronics.velobike.domain.profile

import com.google.gson.annotations.SerializedName

data class Card(
    @SerializedName("CardIDP")
    val cardIdp: String,
    @SerializedName("CardNumber")
    val cardNumber: String,
    @SerializedName("CardType")
    val cardType: String,
    @SerializedName("ExpDate")
    val expirationDate: String,
    @SerializedName("IsDefault")
    val isDefault: Int,
    @SerializedName("Status")
    val status: CardStatus
)

enum class CardStatus(val raw: String) {
    @SerializedName("NOT_APPROVED")
    NOT_APPROVED("NOT_APPROVED"),

    @SerializedName("ACTIVE")
    ACTIVE("ACTIVE"),

    @SerializedName("LOCKED")
    LOCKED("LOCKED")
}