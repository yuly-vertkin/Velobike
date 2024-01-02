package ru.sitronics.velobike.data.repositories.rent

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StartRentParams(
    val bikeSerialNumber: String,
    val isUsedQr: Boolean,
    val clientGeoPosition: ClientGeoPosition,
) : Parcelable

@Parcelize
data class ClientGeoPosition(
    val lat: Double,
    val lon: Double
): Parcelable
