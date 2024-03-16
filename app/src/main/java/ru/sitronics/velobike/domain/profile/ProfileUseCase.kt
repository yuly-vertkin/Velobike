package ru.sitronics.velobike.domain.profile

import ru.sitronics.velobike.R
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.presentation.BaseUseCase
import ru.sitronics.velobike.tools.Logg
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileUseCase  @Inject constructor(
    private val profileRepository: ProfileRepository,
    appContextProvider: AppContextProvider,
) : BaseUseCase(appContextProvider) {

    fun getProfileData(onResult: (Profile) -> Unit) {
        getProfile { profile ->
            getTariff(isOld = true) {
                getTariff(isOld = false) {
                        onResult(profile)
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
        val tariff = if (isOld) profile?.oldTariff else profile?.tariff
        val tariffId = if (isOld) profile?.oldTariffId else profile?.tariffId

        if (tariff != null || tariffId.isNullOrEmpty()) {
            onResult()
        } else {
            processNetworkCall(
                action = { profileRepository.getTariff(tariffId) },
                onSuccess = { tariff ->
                    Logg.d("!!!! getTariff $tariffId success")
                    profile?.let {
                        if (isOld) it.oldTariff = tariff
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

    fun getTariffs(
        onError: (String?) -> Unit, onSuccess: (List<Tariff>) -> Unit
    ) {
        val tariffs = profileRepository.getData().tariffs
        if (tariffs != null) {
            onSuccess(tariffs)
        } else {
            processNetworkCall(
                action = { profileRepository.getTariffs() },
                onSuccess = {
                    Logg.d("!!!! getTariffs success")
                    val profileData = profileRepository.getData().copy(
                        tariffs = it
                    )
                    profileRepository.saveData(profileData)
                    onSuccess(it)
                },
                onError = {
                    Logg.d("!!!! getTariffs error")
                    onError("getTariffs error")
                },
                callName = "getTariffs"
            )
        }
    }

    fun getCards(onResult: (List<Card>) -> Unit) {
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

    fun payTariff(
        tariffId: String, cards: List<Card>,
        onError: (String?) -> Unit, onSuccess: (String?) -> Unit
    ) {
        val cardId = cards.firstOrNull { it.status == CardStatus.ACTIVE && it.isDefault != 0 }?.cardIdp?.toLongOrNull()

        if (cardId == null) {
            onError(context.getString(R.string.error_no_active_card))
            return
        }

        val params = TariffPaymentParams(tariffId, cardId)

        processNetworkCall(
            action = { profileRepository.payTariff(params) },
            onSuccess = {
                Logg.d("!!!! payTariff success")
                onSuccess(it.message)
            },
            onError = {
                Logg.d("!!!! payTariff error")
                onError("payTariff error")
            },
            callName = "payTariff"
        )
    }

    fun calculateRentCost(startTime: Long, isOld: Boolean, onResult: (Int) -> Unit) {
        val timeZoneOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis())
        val duration = System.currentTimeMillis() - timeZoneOffset - startTime * 1000
        val time = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()

        getProfileData { profile ->
            val tariff = if (!isOld) profile.tariff else profile.oldTariff
            val segments =  if (!tariff?.segments.isNullOrEmpty()) tariff?.segments
                            else if (!tariff?.oldSegments.isNullOrEmpty()) tariff?.oldSegments
                            else tariff?.oldElectroSegments
            var cost = 0
            if (time > MAX_RIDE_TIME) {
                cost = FINE_AMOUNT
            } else if (!segments.isNullOrEmpty()) {
                (segments as MutableList).sortBy { it.from }
                segments.forEach {
                    val from = it.from ?: 0
                    val to = it.to ?: 0
                    val segCost = (it.cost ?: 0.0).toInt()

                    if (time > from) {
                        cost += (time.coerceAtMost(to) - from) * segCost
                    }
                }
            }
            onResult(cost)
        }
    }

    companion object {
        private const val MAX_RIDE_TIME = 2800
        private const val FINE_AMOUNT = 30000
    }
}