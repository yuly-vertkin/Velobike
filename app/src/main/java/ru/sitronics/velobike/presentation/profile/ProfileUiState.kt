package ru.sitronics.velobike.presentation.profile

import ru.sitronics.velobike.domain.profile.Profile

sealed class ProfileUiState {
    data class Normal(val profile: Profile) : ProfileUiState()
    data class Error(val error: String?) : ProfileUiState()
}

sealed class ProfileIntent {
    object OnSome : ProfileIntent()
}
