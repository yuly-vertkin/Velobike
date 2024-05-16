package ru.sitronics.velobike.presentation.auth

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.ResponseException
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.domain.auth.AuthRepository
import ru.sitronics.velobike.domain.auth.Register
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
        initNormalState()
    )
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.LoginAction -> {
                loginUser(intent.login, intent.password)
            }
            is LoginIntent.LoginBiometricAction -> {
                val login = authRepository.getData().login ?: ""
                val password = authRepository.getData().password ?: ""

                if (intent.isSuccess)
                    loginUser(login, password)
                else
                    changeState(LoginUiState.Normal(login, password))
            }
            is LoginIntent.ShowRegister -> {
                changeState(LoginUiState.Register)
            }
            is LoginIntent.RegisterAction -> {
                registerUser(intent.registerData)
            }
            is LoginIntent.MessageAction -> {
                changeState(LoginUiState.Normal(
                    login = authRepository.getData().login ?: "",
                    password = authRepository.getData().password ?: "",
                ))
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
        authManager.setToken(response.accessToken)
        changeState(LoginUiState.Close)

        viewModelScope.launch {
            delay(1000)
            changeState(initNormalState())
        }
    }

    private fun onRegisterSuccess(status: Register) {
        Logg.d("!!! register status ${status.code}")
        changeState(LoginUiState.Message(context.getString(
            if (status.isSuccess()) R.string.register_success else R.string.error_unknown)))
    }

    private fun onError(e: Throwable) {
        val text = if (e is ResponseException) e.errorMessage else e.message
        changeState(LoginUiState.Message(text))
    }

    private fun changeState(uiState: LoginUiState) {
        _loginUiState.value = uiState
    }

    private fun initNormalState() = LoginUiState.Normal(
        login = authRepository.getData().login ?: "",
        password = authRepository.getData().password ?: "",
    )
}