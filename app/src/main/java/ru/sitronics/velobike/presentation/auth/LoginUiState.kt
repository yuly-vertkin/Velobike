package ru.sitronics.velobike.presentation.auth

import ru.sitronics.velobike.domain.auth.RegisterData

sealed class LoginUiState {
    data class Normal(val login: String, val password: String) : LoginUiState()
    data class Message(val msg: String?) : LoginUiState()
    data object Register : LoginUiState()
    data object Close : LoginUiState()
}

sealed class LoginIntent {
    data class LoginAction(val login: String, val password: String) : LoginIntent()
    data object ShowRegister : LoginIntent()
    data class RegisterAction(val registerData: RegisterData) : LoginIntent()
    data object MessageAction : LoginIntent()
}
