package ru.sitronics.velobike.presentation.main

sealed class MainUiState {
    data object Splash : MainUiState()
    data object Login : MainUiState()
    data object Normal : MainUiState()
}

sealed class MainIntent {
    data object Logged : MainIntent()
}
