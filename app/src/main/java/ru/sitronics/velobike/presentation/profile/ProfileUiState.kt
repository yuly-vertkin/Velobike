package ru.sitronics.velobike.presentation.profile

import ru.sitronics.velobike.domain.profile.Card
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.Tariff

sealed class ProfileUiState {
    data class Normal(val profile: Profile?) : ProfileUiState()
    data class Tariffs(val tariffs: List<Tariff>) : ProfileUiState()
    data class TariffDetail(val tariff: Tariff, val canBuy: Boolean) : ProfileUiState()
    data class Cards(val cards: List<Card>) : ProfileUiState()
    data class ShowMessage(val title: String, val text: String) : ProfileUiState()
}

sealed class ProfileIntent {
    data object GetTariffs : ProfileIntent()
    data class GetTariff(val tariff: Tariff? = null, val canBuy: Boolean) : ProfileIntent()
    data class BuyTariff(val tariff: Tariff? = null, val canBuy: Boolean) : ProfileIntent()
    data object GetCards : ProfileIntent()
    data object CloseMessage : ProfileIntent()
}
