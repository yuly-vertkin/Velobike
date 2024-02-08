package ru.sitronics.velobike.presentation.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.ProfileRepository
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
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
            is ProfileIntent.GetTariffs -> {
                getTariffs()
            }
            is ProfileIntent.CloseTariffs -> {
                getProfile()
            }
            is ProfileIntent.CloseError -> {
                val profile = profileRepository.getData().profile
                changeState(ProfileUiState.Normal(profile))
            }
        }
    }

    private fun getProfile() {
        val profile = profileRepository.getData().profile
        if (profile != null) {
            changeState(ProfileUiState.Normal(profile))
        } else {
            processNetworkCall(
                action = { profileRepository.getProfile() },
                onSuccess = {
                    Logg.d("!!!! getProfile success")
                    val profileData = profileRepository.getData().copy(
                        profile = it
                    )
                    profileRepository.saveData(profileData)
                    changeState(ProfileUiState.Normal(it))
                },
                onError = {
                    Logg.d("!!!! getProfile error")
                    showError("getProfile error")
                },
                callName = "getProfile"
            )
        }
    }

    private fun getTariffs() {
        val tariffs = profileRepository.getData().tariffs
        if (tariffs != null) {
            changeState(ProfileUiState.Tariffs(tariffs))
        } else {
            processNetworkCall(
                action = { profileRepository.getTariffs() },
//                action = { profileRepository.getTariff("1590") },
                onSuccess = {
                    Logg.d("!!!! getTariffs success")
                    val profileData = profileRepository.getData().copy(
                        tariffs = it
                    )
                    profileRepository.saveData(profileData)
                    changeState(ProfileUiState.Tariffs(it))
                },
                onError = {
                    Logg.d("!!!! getTariffs error")
                    showError("getTariffs error")
                },
                callName = "getTariffs"
            )
        }
    }

    private fun showError(msg: String?) {
        msg?.let { changeState(ProfileUiState.Error(context.getString(R.string.error_title), it)) }
    }

    private fun changeState(uiState: ProfileUiState) {
        _profileUiState.value = uiState
    }
}