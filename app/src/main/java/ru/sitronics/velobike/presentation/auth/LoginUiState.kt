package ru.sitronics.velobike.presentation.auth

sealed class LoginUiState {
    data class Normal(val login: String, val password: String) : LoginUiState()
    data class Error(val error: String?) : LoginUiState()
    object Close : LoginUiState()
}

sealed class LoginIntent {
    data class OnLogin(val login: String, val password: String) : LoginIntent()
    object OnLoginError : LoginIntent()
}
