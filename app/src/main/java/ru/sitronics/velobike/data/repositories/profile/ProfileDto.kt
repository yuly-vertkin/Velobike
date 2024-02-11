package ru.sitronics.velobike.data.repositories.profile

import com.google.gson.annotations.SerializedName
import ru.sitronics.velobike.data.repositories.ResponseDto
import ru.sitronics.velobike.domain.profile.Card
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.Tariff

data class ProfilePackage(
    @SerializedName("Profile")
    val profile: Profile,
) : ResponseDto<Profile> {
    override fun toModel(): Profile = profile
}

data class TariffPackage(
    @SerializedName("Items")
    val items: List<Tariff>
) : ResponseDto<List<Tariff>> {
    override fun toModel(): List<Tariff> = items
}

data class Cards(
    @SerializedName("Items")
    val cards: List<Card>
) : ResponseDto<List<Card>> {
    override fun toModel(): List<Card> = cards
}