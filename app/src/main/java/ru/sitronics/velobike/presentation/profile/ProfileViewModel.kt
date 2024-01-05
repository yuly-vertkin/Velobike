package ru.sitronics.velobike.presentation.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.ProfileData
import ru.sitronics.velobike.domain.profile.ProfileRepository
import ru.sitronics.velobike.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _profileUiState: MutableStateFlow<ProfileUiState> = MutableStateFlow(ProfileUiState.Normal(Profile.empty))
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    init {
        getProfile()
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.OnSome -> {
            }
        }
    }

    private fun getProfile() {
        processNetworkCall(
            action = { profileRepository.getProfile() },
            onSuccess = ::onProfileSuccess,
            onError = ::onProfileError,
        )
    }

    private fun onProfileSuccess(response: Profile) {
        val profileData = ProfileData(response)
        profileRepository.saveData(profileData)

        _profileUiState.value = ProfileUiState.Normal(response)
    }

    private fun onProfileError(e: Throwable) {
    }
}