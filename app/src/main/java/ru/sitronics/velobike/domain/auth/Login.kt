package ru.sitronics.velobike.domain.auth

data class LoginData(
    val login: String?,
    val password: String?,
)

data class Login (
    val token: String,
)