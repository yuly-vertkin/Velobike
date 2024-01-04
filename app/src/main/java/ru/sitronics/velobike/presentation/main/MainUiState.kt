package ru.sitronics.velobike.presentation.main

sealed class MainUiState {
    object Splash : MainUiState()
    object Login : MainUiState()
    object Normal : MainUiState()
}

sealed class MainIntent {
    object Logged : MainIntent()
}
