package ru.sitronics.velobike.domain.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.sitronics.velobike.data.repositories.ResponseDto

data class ProfileData(
    val profile: Profile? = null,
)

@Parcelize
data class ProfilePackage(
    @SerializedName("Profile")
    val profile: Profile,
) : Parcelable, ResponseDto<Profile> {
    override fun toModel(): Profile = profile
}

@Parcelize
data class Profile(
    @SerializedName("FirstName")
    val firstName: String,
    @SerializedName("LastName")
    val lastName: String,
    @SerializedName("Login")
    val login: String,
    @SerializedName("PhoneNumber")
    val phoneNumber: String,
    @SerializedName("Email")
    val email: String,
    @SerializedName("TroikaPrintCardNumber")
    val troikaPrintCardNumber: String? = "",
    @SerializedName("TroikaCardNumber")
    val troikaCardNumber: String? = "",
    @SerializedName("NewPin")
    val newPin: String? = "",
    @SerializedName("UserId")
    val userId: String,
    @SerializedName("AvatarUrl")
    val avatarUrl: String = "",
//    @SerializedName("IsAllowPush")
//    val isAllowPush: Boolean = true,
//    @SerializedName("RegisterDate")
//    val registerDate: Date?,
//    @SerializedName("Birthday")
//    val birthday: String,
//    @SerializedName("Balance")
//    val balance: Double,
//    @SerializedName("TariffId")
//    val tariffId: String? = "",
//    @SerializedName("TariffStart")
//    val tariffStart: Date? = null,
//    @SerializedName("TariffEnd")
//    val tariffEnd: Date? = null,
//    @SerializedName("PaymentCardMask")
//    val paymentCardMask: String? = "",
//    @SerializedName("PaymentBind")
//    val paymentBind: String? = "",
//    @SerializedName("TariffName")
//    val tariffName: String? = "",
//    @SerializedName("OmniTariffId")
//    val omniTariffId: String? = "",
//    @SerializedName("OmniTariffStart")
//    val omniTariffStart: Date? = null,
//    @SerializedName("OmniTariffEnd")
//    val omniTariffEnd: Date? = null,
//    @SerializedName("OmniTariffName")
//    val omniTariffName: String? = "",
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
) : Parcelable{
    companion object{
        val empty = Profile("", "", "", "", "", "",
            "", "", "", ""/*, false, Date(), "" +
                    "", 0.0, "",
            null, null, "", "", "", fullNumberRental = 0, omniFullNumberRental = 0, activationRcId = 0, omniActivationRcId = 0*/)
    }
}