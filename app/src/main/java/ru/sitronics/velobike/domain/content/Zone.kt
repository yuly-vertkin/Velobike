package ru.sitronics.velobike.domain.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SlowZone(
    val groupId: Int,
    val speedLimit: Int,
    val geomGeo: GeomGeo,
    val mondayStart: Int,
    val mondayEnd: Int,
    val tuesdayStart: Int,
    val tuesdayEnd: Int,
    val wednesdayStart: Int,
    val wednesdayEnd: Int,
    val thursdayStart: Int,
    val thursdayEnd: Int,
    val fridayStart: Int,
    val fridayEnd: Int,
    val saturdayStart: Int,
    val saturdayEnd: Int,
    val sundayStart: Int,
    val sundayEnd: Int,
    val flagElasticSchedule: Int,
)

data class MoveZone (
    val id: Int,
    val geomGeo: GeomGeo,
)

@Parcelize
data class GeomGeo(
    @SerializedName("coordinates")
    val coordinates: List<List<List<Double>>>,
    @SerializedName("type")
    val type: String
): Parcelable
