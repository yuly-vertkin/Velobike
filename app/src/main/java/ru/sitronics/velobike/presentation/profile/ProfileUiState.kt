package ru.sitronics.velobike.presentation.profile

import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.Tariff

sealed class ProfileUiState {
    data class Normal(val profile: Profile?) : ProfileUiState()
    data class Tariffs(val tariffs: List<Tariff>) : ProfileUiState()
    data class Error(val title: String, val text: String) : ProfileUiState()
}

sealed class ProfileIntent {
    object GetTariffs : ProfileIntent()
    data class CloseTariffs(val tariff: Tariff? = null) : ProfileIntent()
    object CloseError : ProfileIntent()
}
