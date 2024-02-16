package ru.sitronics.velobike.domain.auth

import com.google.gson.annotations.SerializedName

data class AuthData(
    val login: String?,
    val password: String?,
    val registerData: RegisterData? = null,
)

data class UserToken (
    @SerializedName("token")
    val accessToken: String,
    @SerializedName("nmc_access_token")
    val accessTokenOldApi: String,
    @SerializedName("nmc_user_id")
    val userId: String
)

data class RegisterData(
    @SerializedName("FirstName")
    val firstName : String,
    @SerializedName("LastName")
    val lastName : String,
    @SerializedName("PhoneNumber")
    val phoneNumber : String,
    @SerializedName("Email")
    val email : String,
)

data class Register(
    val code: String
) {
    fun isSuccess() : Boolean =
        code == "OK"
}
