package ru.sitronics.velobike.domain.profile

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ProfileData(
    val profile: Profile? = null,
    val tariffs: List<Tariff>? = null,
    val cards: List<Card>? = null,
)

data class Profile(
    @SerializedName("UserId")
    val userId: String = "",
    @SerializedName("FirstName")
    val firstName: String = "",
    @SerializedName("LastName")
    val lastName: String = "",
    @SerializedName("Login")
    val login: String = "",
    @SerializedName("NewPin")
    val pin: String = "",
    @SerializedName("AvatarUrl")
    val avatarUrl: String = "",
    @SerializedName("PhoneNumber")
    val phoneNumber: String = "",
    @SerializedName("Email")
    val email: String = "",
    @SerializedName("TroikaPrintCardNumber")
    val troikaPrintCardNumber: String = "",
    @SerializedName("TroikaCardNumber")
    val troikaCardNumber: String = "",
//    @SerializedName("IsAllowPush")
//    val isAllowPush: Boolean = true,
//    @SerializedName("RegisterDate")
//    val registerDate: Date?,
//    @SerializedName("Birthday")
//    val birthday: String,
//    @SerializedName("Balance")
//    val balance: Double,
    @SerializedName("TariffId")
    val oldTariffId: String = "",
    @SerializedName("TariffName")
    val oldTariffName: String = "",
//    @SerializedName("TariffStart")
//    val oldTariffStart: Date? = null,
    @SerializedName("TariffEnd")
    val oldTariffEnd: Date? = null,
//    @SerializedName("PaymentCardMask")
//    val paymentCardMask: String? = "",
//    @SerializedName("PaymentBind")
//    val paymentBind: String? = "",
    @SerializedName("OmniTariffId")
    val tariffId: String = "",
    @SerializedName("OmniTariffName")
    val tariffName: String = "",
//    @SerializedName("OmniTariffStart")
//    val tariffStart: Date? = null,
    @SerializedName("OmniTariffEnd")
    val tariffEnd: Date? = null,
//    @SerializedName("ActivationFlag")
//    val activationFlag: Boolean = false,
//    @SerializedName("OmniActivationFlag")
//    val omniActivationFlag: Boolean = false,
//    @SerializedName("FullNumberRental")
//    val fullNumberRental: Int,
//    @SerializedName("OmniFullNumberRental")
//    val omniFullNumberRental: Int,
//    @SerializedName("ActivationRcId")
//    val activationRcId: Int,
//    @SerializedName("OmniActivationRcId")
//    val omniActivationRcId: Int,
//    @SerializedName("ActivationTariffEndDate")
//    val activationTariffEndDate: String? = "",
//    @SerializedName("OmniActivationTariffEndDate")
//    val omniActivationTariffEndDate: String? = ""
    var oldTariff: Tariff? = null,
    var tariff: Tariff? = null,
)