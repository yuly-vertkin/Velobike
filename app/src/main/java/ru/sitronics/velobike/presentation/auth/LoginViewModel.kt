package ru.sitronics.velobike.presentation.auth

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.AuthManager
import ru.sitronics.velobike.data.ResponseException
import ru.sitronics.velobike.domain.auth.AuthRepository
import ru.sitronics.velobike.domain.auth.RegisterData
import ru.sitronics.velobike.domain.auth.UserToken
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _loginUiState: MutableStateFlow<LoginUiState> = MutableStateFlow(
        LoginUiState.Normal(
            login = authRepository.getData().login ?: "",
            password = authRepository.getData().password ?: "",
    ))
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnLogin -> {
                loginUser(intent.login, intent.password)
            }
            is LoginIntent.ShowRegister -> {
                _loginUiState.value = LoginUiState.ShowRegister
            }
            is LoginIntent.OnRegister -> {
                registerUser(intent.registerData)
            }
            is LoginIntent.OnMessage -> {
                _loginUiState.value = LoginUiState.Normal(
                    login = authRepository.getData().login ?: "",
                    password = authRepository.getData().password ?: "",
                )
            }
        }
    }

    private fun loginUser(login: String, password: String) {
        authRepository.saveData(authRepository.getData().copy(
            login = login,
            password = password
        ))

        processNetworkCall(
            action = { authRepository.login(login, password) },
            onSuccess = ::onLoginSuccess,
            onError = ::onError,
        )
    }

    private fun registerUser(registerData: RegisterData) {
        authRepository.saveData(authRepository.getData().copy(
            registerData = registerData
        ))

        processNetworkCall(
            action = { authRepository.register(registerData) },
            onSuccess = ::onRegisterSuccess,
            onError = ::onError,
        )
    }

    private fun onLoginSuccess(response: UserToken) {
        Logg.d("!!! Login success")
        authManager.accessToken = response.accessToken
        _loginUiState.value = LoginUiState.Close
    }

    private fun onRegisterSuccess(text: String) {
        Logg.d("!!! register success")
        _loginUiState.value = LoginUiState.ShowMessage(text)
    }

    private fun onError(e: Throwable) {
        val text = if (e is ResponseException) e.errorMessage else e.message
        _loginUiState.value = LoginUiState.ShowMessage(text)
    }
}