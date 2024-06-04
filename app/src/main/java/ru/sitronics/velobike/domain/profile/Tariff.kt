package ru.sitronics.velobike.domain.profile

import com.google.gson.annotations.SerializedName

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
    val oldSegments: List<TariffSegment>?,
    @SerializedName("ElectroSegments")
    val oldElectroSegments: List<TariffSegment>?,
    @SerializedName("OmniSegments")
    val segments: List<TariffSegment>?,
    @SerializedName("AdditionalSegments")
    val additionalOldSegments: List<TariffSegment>?,
    @SerializedName("AdditionalElectroSegments")
    val additionalOldElectroSegments: List<TariffSegment>?,
    @SerializedName("AdditionalOmniSegments")
    val additionalSegments: List<TariffSegment>?,
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
    val id: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("From")
    val from: Int? = null,
    @SerializedName("To")
    val to: Int? = null,
    @SerializedName("Cost")
    val cost: Double? = null
)

data class LKP(
    @SerializedName("LKPTariffId")
    val id: Int
)

data class TariffPaymentParams(
    @SerializedName("rate_id")
    val tariffId : String,
    @SerializedName("card_idp")
    val cardId: Long
)

data class TariffPayment(
    @SerializedName("extCode")
    val extCode : Int,
    @SerializedName("message")
    val message : String?
)
