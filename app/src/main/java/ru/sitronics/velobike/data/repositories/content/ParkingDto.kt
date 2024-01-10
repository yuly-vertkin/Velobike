package ru.sitronics.velobike.data.repositories.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.sitronics.velobike.data.repositories.ResponseDto
import ru.sitronics.velobike.domain.content.Parking
import ru.sitronics.velobike.domain.content.StationType

@Parcelize
data class ParkingDto(
    @SerializedName("static_firestore_id")
    val staticFirestoreId: String?,
    @SerializedName("dynamic_firestore_id")
    val dynamicFirestoreId: String?,
    @SerializedName("addr")
    val address: String?,
    @SerializedName("addr_en")
    val addressEn: String?,
    @SerializedName("icon")
    val icon: Int?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("station_overflowed")
    val stationOverflowed: Boolean?,
    @SerializedName("template_card_id")
    val templateCardId: Int?,
    @SerializedName("payment_terminal_exists")
    val paymentTerminalExists: Boolean?,
    @SerializedName("type")
    val type: Int?,
    @SerializedName("num_of_non_electric_slots")
    val numOfNonElectricSlots: Int?,
    @SerializedName("num_of_electric_slots")
    val numOfElectricSlots: Int?,
    @SerializedName("num_of_omni_slots")
    val numOfOmniSlots: Int?,
    @SerializedName("num_of_both_electric_and_non_electric_compatible_slots")
    val numOfBothElectricAndNonElectricCompatibleSlots: Int?,
    @SerializedName("num_of_free_non_electric_slots")
    val numOfFreeNonElectricSlots: Int?,
    @SerializedName("num_of_free_electric_slots")
    val numOfFreeElectricSlots: Int?,
    @SerializedName("num_of_free_omni_slots")
    val numOfFreeOmniSlots: Int?,
    @SerializedName("num_of_free_both_electric_and_non_electric_compatible_slots")
    val numOfFreeBothElectricAndNonElectricCompatibleSlots: Int?,
    @SerializedName("num_of_available_non_electric_bikes")
    val numOfAvailableNonElectricBikes: Int?,
    @SerializedName("num_of_available_electric_bikes")
    val numOfAvailableElectricBikes: Int?,
    @SerializedName("num_of_available_omni_bikes")
    val numOfAvailableOmniBikes: Int?,
    @SerializedName("station_status")
    val stationStatus: Int?,
) : Parcelable, ResponseDto<Parking> {
    override fun toModel(): Parking {
        val stType = StationType.values().firstOrNull { st -> st.type == type } ?: StationType.Ordinary

        return Parking(
            id = staticFirestoreId ?: "",
            type = stType,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
            address = address ?: "",
            nonElectricSlots = numOfNonElectricSlots ?: 0,
            electricSlots = numOfElectricSlots ?: 0,
            omniSlots = numOfOmniSlots ?: 0,
            freeNonElectricSlots = numOfFreeNonElectricSlots ?: 0,
            freeElectricSlots = numOfFreeElectricSlots ?: 0,
            freeOmniSlots = numOfFreeOmniSlots ?: 0,
            availableNonElectricBikes = numOfAvailableNonElectricBikes ?: 0,
            availableElectricBikes = numOfAvailableElectricBikes ?: 0,
            availableOmniBikes = numOfAvailableOmniBikes ?: 0,
            status = stationStatus ?: 0,
        )
    }
}
