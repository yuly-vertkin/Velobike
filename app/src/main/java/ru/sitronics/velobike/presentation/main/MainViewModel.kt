package ru.sitronics.velobike.presentation.main

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authManager: AuthManager,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _mainUiState: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState.Splash)
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_DELAY)
            changeState(if (authManager.isLogged) MainUiState.Normal else MainUiState.Login)
        }

        authManager.reLoginListener = { changeState(MainUiState.Login) }
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.Logged -> changeState(MainUiState.Normal)
        }
    }

    private fun changeState(uiState: MainUiState) {
        _mainUiState.value = uiState
    }

    companion object {
        private const val SPLASH_DELAY = 750L
    }
}