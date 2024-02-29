package ru.sitronics.velobike.domain.auth

interface AuthManager {
    val accessToken: String?
    val accessTokenOldApi: String?
    val userId: String?
    val isLogged: Boolean
    var reLoginListener: (() -> Unit)?
    fun needReLogin()
    fun setToken(token: String?)
}