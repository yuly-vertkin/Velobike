package ru.sitronics.velobike.domain.auth

import com.google.gson.annotations.SerializedName

data class LoginData(
    val login: String?,
    val password: String?,
)

data class UserToken (
    @SerializedName("token")
    val accessToken: String,
    @SerializedName("nmc_access_token")
    val accessTokenOldApi: String,
)