package ru.sitronics.velobike.data.repositories.profile

import com.google.gson.annotations.SerializedName
import ru.sitronics.velobike.data.repositories.ResponseDto
import ru.sitronics.velobike.domain.profile.Tariff

data class TariffPackage(
    @SerializedName("Items")
    val items: List<Tariff>
) : ResponseDto<List<Tariff>> {
    override fun toModel(): List<Tariff> = items
}
