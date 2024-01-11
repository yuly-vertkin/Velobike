package ru.sitronics.velobike.presentation.auth

import ru.sitronics.velobike.domain.auth.RegisterData

sealed class LoginUiState {
    data class Normal(val login: String, val password: String) : LoginUiState()
    data class ShowMessage(val msg: String?) : LoginUiState()
    object ShowRegister : LoginUiState()
    object Close : LoginUiState()
}

sealed class LoginIntent {
    data class OnLogin(val login: String, val password: String) : LoginIntent()
    object ShowRegister : LoginIntent()
    data class OnRegister(val registerData: RegisterData) : LoginIntent()
    object OnMessage : LoginIntent()
}
