package ru.sitronics.velobike.presentation.auth

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.data.AuthManager
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.ResponseException
import ru.sitronics.velobike.domain.auth.LoginData
import ru.sitronics.velobike.domain.auth.LoginRepository
import ru.sitronics.velobike.domain.auth.Login
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val authManager: AuthManager,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _loginUiState: MutableStateFlow<LoginUiState> = MutableStateFlow(
        LoginUiState.Normal(
            login = loginRepository.getData().login ?: "",
            password = loginRepository.getData().password ?: "",
    ))
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnLogin -> {
                loginUser(intent.login, intent.password)
            }
            is LoginIntent.OnLoginError -> {
                _loginUiState.value = LoginUiState.Normal(
                    login = loginRepository.getData().login ?: "",
                    password = loginRepository.getData().password ?: "",
                )
            }
        }
    }

    private fun loginUser(login: String, password: String) {
        val loginData = LoginData(login, password)
        loginRepository.saveData(loginData)

        processNetworkCall(
            action = { loginRepository.login(login, password) },
            onSuccess = ::onLoginSuccess,
            onError = ::onLoginError,
        )
    }

    private fun onLoginSuccess(response: Login) {
        Logg.d("!!! Login success")
        authManager.token = response.token
        _loginUiState.value = LoginUiState.Close
    }

    private fun onLoginError(e: Throwable) {
        Logg.d("!!! Login error")
        val text = if (e is ResponseException) e.errorMessage else e.message
        _loginUiState.value = LoginUiState.Error(text)
    }
}