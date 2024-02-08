package ru.sitronics.velobike.domain.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.sitronics.velobike.data.repositories.ResponseDto

data class ProfileData(
    val profile: Profile? = null,
    val tariffs: List<Tariff>? = null,
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

data class Tariff(
    @SerializedName("Id")
    val id: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Cost")
    val cost: Double? = 0.0,
    @SerializedName("Amount")
    val amount: Double? = 0.0,
    @SerializedName("Description")
    val description: String?,
    @SerializedName("DescriptionForList")
    val descriptionForList: String?,
    @SerializedName("Insurance")
    val insurance: Insurance?,
    @SerializedName("Icon")
    val icon: String?,
    @SerializedName("RentalContractBikeType")
    val tariffBikeType: TariffBikeType?,
    @SerializedName("Segments")
    val segments: List<TariffSegment>?,
    @SerializedName("ElectroSegments")
    val electroSegments: List<TariffSegment>?,
    @SerializedName("AdditionalSegments")
    val additionalSegments: List<TariffSegment>?,
    @SerializedName("AdditionalElectroSegments")
    val additionalElectroSegments: List<TariffSegment>?,
    @SerializedName("AdditionalOmniSegments")
    val additionalOmniSegments: List<TariffSegment>?,
    @SerializedName("OmniSegments")
    val omniSegments: List<TariffSegment>?,
    @SerializedName("ByMinute")
    val byMinute: Boolean,
    @SerializedName("BillingDescription")
    val billingDescription: String?,
    @SerializedName("BillingDetails")
    val billingDetails: String,
    @SerializedName("TripsNoTitle")
    val tripsNoTitle: String?,
    @SerializedName("TripsNoText")
    val tripsNoText: String?,
    @SerializedName("ExpirationTitle")
    val expirationTitle: String?,
    @SerializedName("ExpirationText")
    val expirationText: String?,
    @SerializedName("ShareTitle")
    val shareTitle: String?,
    @SerializedName("ShareText")
    val shareText: String?,
    @SerializedName("FinesTitle")
    val finesTitle: String?,
    @SerializedName("FinesText")
    val finesText: String?,
    @SerializedName("MaxBikes")
    val maxBikes: Int = 1,
    @SerializedName("ExtraInfo")
    val extraInfo: String?,
    @SerializedName("PaymentTerms")
    val paymentTerms: String?,
    @SerializedName("PriceDescription")
    val priceDescription: String?,
    @SerializedName("LKP")
    val lkp: List<LKP>,
    @SerializedName("NumberOfDisable")
    val numberOfDisable: Int,
    @SerializedName("OmniNumberOfDisable")
    val omniNumberOfDisable: Int,
    @SerializedName("IsDisabledByNumRen")
    val isDisabledByNumRen: Boolean
)

enum class TariffBikeType(val value: String) {
    @SerializedName("FOR_ORDINARY_AND_ELECTRIC_BIKE")
    ORDINARY_AND_ELECTRIC("FOR_ORDINARY_AND_ELECTRIC_BIKE"),

    @SerializedName("FOR_OMNI_BIKE")
    OMNI("FOR_OMNI_BIKE");
}

data class Insurance(
    @SerializedName("InsuranceId")
    val id : String,
    @SerializedName("Cost")
    val cost : Double?,
    @SerializedName("InsuranceCost")
    val insuranceCost : Double?,
    @SerializedName("Type")
    val type : String,
    @SerializedName("DescriptionRu")
    val descriptionRu : String?,
    @SerializedName("DescriptionEn")
    val descriptionEn : String?,
    @SerializedName("Segments")
    val segments : List<TariffSegment>?,
    @SerializedName("ElectroSegments")
    val electroSegments : List<TariffSegment>?,
    @SerializedName("ByMinute")
    val byMinute: Boolean,
    @SerializedName("BillingDescription")
    val billingDescription: String?,
    @SerializedName("BillingDetails")
    val billingDetails: String?,
    @SerializedName("TripsNoTitle")
    val tripsNoTitle: String?,
    @SerializedName("TripsNoText")
    val tripsNoText: String?,
    @SerializedName("ExpirationTitle")
    val expirationTitle: String?,
    @SerializedName("ExpirationText")
    val expirationText: String?,
    @SerializedName("ShareTitle")
    val shareTitle: String?,
    @SerializedName("ShareText")
    val shareText: String?,
    @SerializedName("FinesTitle")
    val finesTitle: String?,
    @SerializedName("FinesText")
    val finesText: String?
)

data class TariffSegment(
    @SerializedName("Id")
    val id : String,
    @SerializedName("Name")
    val name : String,
    @SerializedName("From")
    val from : Int? = null,
    @SerializedName("To")
    val to : Int? = null,
    @SerializedName("Cost")
    val cost : Double? = null
)

data class LKP(
    @SerializedName("LKPTariffId")
    val id: Int
)