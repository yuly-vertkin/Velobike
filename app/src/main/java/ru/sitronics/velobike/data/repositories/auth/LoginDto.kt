package ru.sitronics.velobike.data.repositories.auth

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginDto(
    @SerializedName("user")
    val user: String,
    @SerializedName("password")
    val password: String,
) : Parcelable
