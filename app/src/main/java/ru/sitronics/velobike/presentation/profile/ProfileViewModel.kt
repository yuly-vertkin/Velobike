package ru.sitronics.velobike.presentation.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.domain.profile.Card
import ru.sitronics.velobike.domain.profile.CardStatus
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.ProfileRepository
import ru.sitronics.velobike.domain.profile.TariffPaymentParams
import ru.sitronics.velobike.presentation.BaseViewModel
import ru.sitronics.velobike.tools.Logg
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    appContextProvider: AppContextProvider,
) : BaseViewModel(appContextProvider) {
    private val _profileUiState: MutableStateFlow<ProfileUiState> = MutableStateFlow(ProfileUiState.Normal(Profile()))
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    init {
        getProfileData()
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.GetTariffs -> {
                getTariffs()
            }
            is ProfileIntent.GetTariff -> {
                intent.tariff?.let {
                    changeState(ProfileUiState.TariffDetail(it, intent.canBuy))
                } ?: getProfileData()
            }
            is ProfileIntent.BuyTariff -> {
                if (intent.tariff != null)
                    getCards {
                        payTariff(intent.tariff.id, it)
                    }
                else if (intent.canBuy) getTariffs()
                else getProfileData()
            }
            is ProfileIntent.GetCards -> {
                getCards {
                    changeState(ProfileUiState.Cards(it))
                }
            }
            is ProfileIntent.CloseMessage -> {
                val profile = profileRepository.getData().profile
                changeState(ProfileUiState.Normal(profile))
            }
        }
    }

    private fun getProfileData() {
        getProfile { profile ->
            getTariff(isOld = true) {
                getTariff(isOld = false) {
                    changeState(ProfileUiState.Normal(profile))
                }
            }
        }
    }

    private fun getProfile(onResult: (Profile) -> Unit) {
        val profile = profileRepository.getData().profile
        if (profile != null) {
            onResult(profile)
        } else {
            processNetworkCall(
                action = { profileRepository.getProfile() },
                onSuccess = {
                    Logg.d("!!!! getProfile success")
                    val profileData = profileRepository.getData().copy(
                        profile = it
                    )
                    profileRepository.saveData(profileData)
                    onResult(it)
                },
                onError = {
                    Logg.d("!!!! getProfile error")
                    onResult(Profile())
                },
                callName = "getProfile"
            )
        }
    }

    private fun getTariff(isOld: Boolean, onResult: () -> Unit) {
        val profile = profileRepository.getData().profile
        val tariff = if (isOld) profile?.tariffOld else profile?.tariff
        val tariffId = if (isOld) profile?.tariffIdOld else profile?.tariffId

        if (tariff != null || tariffId.isNullOrEmpty()) {
            onResult()
        } else {
            processNetworkCall(
                action = { profileRepository.getTariff(tariffId) },
                onSuccess = { tariff ->
                    Logg.d("!!!! getTariff $tariffId success")
                    profile?.let {
                        if (isOld) it.tariffOld = tariff
                        else       it.tariff = tariff
                    }
                    onResult()
                },
                onError = {
                    Logg.d("!!!! getTariff $tariffId error")
                    onResult()
                },
                callName = "getTariff $tariffId"
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

    private fun getCards(onResult: (List<Card>) -> Unit) {
        val cards = profileRepository.getData().cards
        if (cards != null) {
            onResult(cards)
        } else {
            processNetworkCall(
                action = { profileRepository.getCards() },
                onSuccess = {
                    Logg.d("!!!! getCards success")
                    val profileData = profileRepository.getData().copy(
                        cards = it
                    )
                    profileRepository.saveData(profileData)
                    onResult(it)
                },
                onError = {
                    Logg.d("!!!! getCards error")
                    onResult(emptyList())
                },
                callName = "getCards"
            )
        }
    }

    private fun payTariff(tariffId: String, cards: List<Card>) {
        val cardId = cards.firstOrNull { it.status == CardStatus.ACTIVE && it.isDefault != 0 }?.cardIdp?.toLongOrNull()

        if (cardId == null) {
            showError(context.getString(R.string.error_no_active_card))
            return
        }

        val params = TariffPaymentParams(tariffId, cardId)

        processNetworkCall(
            action = { profileRepository.payTariff(params) },
            onSuccess = {
                Logg.d("!!!! payTariff success")
                changeState(ProfileUiState.ShowMessage(context.getString(R.string.success), it.message.orEmpty()))
            },
            onError = {
                Logg.d("!!!! payTariff error")
                showError("payTariff error")
            },
            callName = "payTariff"
        )
    }

    private fun showError(msg: String?) {
        msg?.let { changeState(ProfileUiState.ShowMessage(context.getString(R.string.error_title), it)) }
    }

    private fun changeState(uiState: ProfileUiState) {
        _profileUiState.value = uiState
    }
}