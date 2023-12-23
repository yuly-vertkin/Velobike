package ru.sitronics.velobike.domain.content

data class Parking(
    val id: String,
    val type: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val nonElectricSlots: Int,
    val electricSlots: Int,
    val omniSlots: Int,
    val freeNonElectricSlots: Int,
    val freeElectricSlots: Int,
    val freeOmniSlots: Int,
    val status: Int,
)
