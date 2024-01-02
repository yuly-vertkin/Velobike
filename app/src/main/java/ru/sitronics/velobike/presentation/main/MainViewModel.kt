package ru.sitronics.velobike.presentation.main

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.AuthManager
import ru.sitronics.velobike.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authManager: AuthManager,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _mainUiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        if (authManager.isLogged) MainUiState.Normal else MainUiState.Login
    )
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.Logged -> _mainUiState.value = MainUiState.Normal
        }
    }
}