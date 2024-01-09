package ru.sitronics.velobike.domain.content

data class Parking(
    val id: String,
    val type: StationType,
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

enum class StationType(val type: Int) {
    Ordinary(0),
    Electro(1),
    OrdinaryAndElectro(2),
    Omni(3);

    fun isParking(): Boolean {
        return this == Omni
    }

    fun isStation(): Boolean {
        return this == Ordinary || this == Electro || this == OrdinaryAndElectro
    }
}