package ru.sitronics.velobike.data.repositories.auth

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.sitronics.velobike.domain.auth.RegisterData

@Parcelize
data class LoginDto(
    @SerializedName("user")
    val user: String,
    @SerializedName("password")
    val password: String,
) : Parcelable

data class RegisterParams (
    @SerializedName("profileData")
    val registerData: RegisterData
)
