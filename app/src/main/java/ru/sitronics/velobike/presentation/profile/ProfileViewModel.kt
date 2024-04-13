package ru.sitronics.velobike.presentation.profile

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.ProfileUseCase
import ru.sitronics.velobike.presentation.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileUseCase: ProfileUseCase,
    private val authManager: AuthManager,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _profileUiState: MutableStateFlow<ProfileUiState> = MutableStateFlow(ProfileUiState.Normal(Profile()))
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    init {
        profileUseCase.initScope(viewModelScope)

        profileUseCase.getProfileData {
            changeState(ProfileUiState.Normal(it))
        }
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.GetTariffs -> {
                profileUseCase.getTariffs({ showError(it) }) {
                    changeState(ProfileUiState.Tariffs(it))
                }
            }
            is ProfileIntent.GetTariff -> {
                intent.tariff?.let {
                    changeState(ProfileUiState.TariffDetail(it, intent.canBuy))
                } ?: profileUseCase.getProfileData {
                    changeState(ProfileUiState.Normal(it))
                }
            }
            is ProfileIntent.BuyTariff -> {
                if (intent.tariff != null)
                    profileUseCase.getCards { cards ->
                        profileUseCase.payTariff(intent.tariff.id, cards, { showError(it) }) {
                            changeState(ProfileUiState.Message(context.getString(R.string.success), it.orEmpty()))
                        }
                    }
                else if (intent.canBuy)
                    profileUseCase.getTariffs({ showError(it) }) {
                        changeState(ProfileUiState.Tariffs(it))
                    }
                else
                    profileUseCase.getProfileData {
                        changeState(ProfileUiState.Normal(it))
                    }
            }
            is ProfileIntent.GetCards -> {
                profileUseCase.getCards {
                    changeState(ProfileUiState.Cards(it))
                }
            }
            is ProfileIntent.MessageAction -> {
                profileUseCase.getProfileData {
                    changeState(ProfileUiState.Normal(it))
                }
            }
            is ProfileIntent.Logout -> {
                authManager.needReLogin()
            }
        }
    }

    private fun showError(msg: String?) {
        msg?.let { changeState(ProfileUiState.Message(context.getString(R.string.error_title), it)) }
    }

    private fun changeState(uiState: ProfileUiState) {
        _profileUiState.value = uiState
    }
}