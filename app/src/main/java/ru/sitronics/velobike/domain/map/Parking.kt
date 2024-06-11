package ru.sitronics.velobike.domain.map

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
    val availableNonElectricBikes: Int,
    val availableElectricBikes: Int,
    val availableOmniBikes: Int,
    val isLocked: Boolean,
    var distance: Float? = null,
) {
    companion object {
        fun empty() =
            Parking("", StationType.Omni, 0.0, 0.0, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, false)
    }
}

enum class StationType(val type: Int) {
    Ordinary(0),
    Electro(1),
    OrdinaryAndElectro(2),
    Omni(3);

    fun isParking() : Boolean =
        this == Omni

    fun isStation() : Boolean =
        this == Ordinary || this == Electro || this == OrdinaryAndElectro

    fun isElectro() : Boolean =
        this == Electro || this == OrdinaryAndElectro

}