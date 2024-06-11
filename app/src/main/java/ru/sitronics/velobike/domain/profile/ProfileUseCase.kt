package ru.sitronics.velobike.domain.profile

import android.content.Context
import ru.sitronics.velobike.R
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.presentation.BaseUseCase
import ru.sitronics.velobike.presentation.BaseUseCaseImp
import ru.sitronics.velobike.tools.Logg
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileUseCase : BaseUseCase {
    fun getProfileData(onResult: (Profile) -> Unit)
    fun getProfileFromCache() : Profile?
    fun getTariffs(onError: (String?) -> Unit, onSuccess: (List<Tariff>) -> Unit)
    fun getCards(onResult: (List<Card>) -> Unit)
    fun payTariff(tariffId: String, cards: List<Card>, onError: (String?) -> Unit, onSuccess: (String?) -> Unit)
    fun getLKPStatus(onResult: (LKPStatus) -> Unit)
    fun authInMetro(onError: (String?) -> Unit, onSuccess: (MetroPasswordParameters) -> Unit)
    fun createAuthToken(code: String, onError: (String?) -> Unit, onSuccess: () -> Unit)
    fun calculateRentCost(startTime: Long, isOld: Boolean, onResult: (Int) -> Unit)
    fun isFine(startTime: Long) : Boolean
    fun isAccountLinked() : Boolean
    fun tariffIsActive() : Boolean
}

@Singleton
class ProfileUseCaseImp @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authManager: AuthManager,
    appContext: Context,
) : BaseUseCaseImp(appContext), ProfileUseCase {

    override fun getProfileData(onResult: (Profile) -> Unit) {
        getProfile { profile ->
            getTariff(isOld = true) {
                getTariff(isOld = false) {
                        onResult(profile)
                }
            }
        }
    }

    override fun getProfileFromCache() =
        profileRepository.getData().profile

    private fun getProfile(update: Boolean = false, onResult: (Profile) -> Unit) {
        val profile = profileRepository.getData().profile
        if (profile != null && !update) {
            onResult(profile)
        } else {
            processNetworkCall(
                action = { profileRepository.getProfile() },
                onSuccess = {
                    val profileData = profileRepository.getData().copy(
                        profile = it
                    )
                    profileRepository.saveData(profileData)
                    onResult(it)
                },
                onError = { onResult(Profile()) },
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
                    profile?.let {
                        if (isOld) it.oldTariff = tariff
                        else       it.tariff = tariff
                    }
                    onResult()
                },
                onError = { onResult() },
                callName = "getTariff $tariffId"
            )
        }
    }

    override fun getTariffs(
        onError: (String?) -> Unit, onSuccess: (List<Tariff>) -> Unit
    ) {
        val tariffs = profileRepository.getData().tariffs
        if (tariffs != null) {
            onSuccess(tariffs)
        } else {
            processNetworkCall(
                action = { profileRepository.getTariffs() },
                onSuccess = {
                    val profileData = profileRepository.getData().copy(
                        tariffs = it
                    )
                    profileRepository.saveData(profileData)
                    onSuccess(it)
                },
                onError = { onError(null) },
                callName = "getTariffs"
            )
        }
    }

    override fun getCards(onResult: (List<Card>) -> Unit) {
        val cards = profileRepository.getData().cards
        if (cards != null) {
            onResult(cards)
        } else {
            processNetworkCall(
                action = { profileRepository.getCards() },
                onSuccess = {
                    val profileData = profileRepository.getData().copy(
                        cards = it
                    )
                    profileRepository.saveData(profileData)
                    onResult(it)
                },
                onError = { onResult(emptyList()) },
                callName = "getCards"
            )
        }
    }

    override fun payTariff(
        tariffId: String, cards: List<Card>,
        onError: (String?) -> Unit, onSuccess: (String?) -> Unit
    ) {
        val cardId = cards.firstOrNull { it.status == CardStatus.ACTIVE && it.isDefault != 0 }?.cardIdp?.toLongOrNull()

        if (cardId == null) {
            onError(appContext.getString(R.string.error_no_active_card))
            return
        }

        val params = TariffPaymentParams(tariffId, cardId)

        processNetworkCall(
            action = { profileRepository.payTariff(params) },
            onSuccess = {
                val message = it.message
                Logg.d("!!!! payTariff: $message")
                getProfile(update = true) {
                    onSuccess(message)
                }
            },
            onError = { onError(null) },
            callName = "payTariff"
        )
    }

    override fun getLKPStatus(onResult: (LKPStatus) -> Unit) {
        authManager.userId?.let { id ->
            processNetworkCall(
                action = { profileRepository.getLKPStatus(id) },
                onSuccess = { onResult(it.status) },
                onError = { onResult(LKPStatus.NONE) },
                callName = "getLKPStatus"
            )
        }
    }

    override fun authInMetro(onError: (String?) -> Unit, onSuccess: (MetroPasswordParameters) -> Unit) {
        authManager.userId?.let { id ->
            getProfileFromCache()?.let { profile ->
                processNetworkCall(
                    action = { profileRepository.authInMetro(id, profile.phoneNumber) },
                    onSuccess = { onSuccess(it) },
                    onError = { onError(appContext.getString(R.string.error_unknown_later)) },
                    callName = "authInMetro"
                )
            }
        }
    }

    override fun createAuthToken(code: String, onError: (String?) -> Unit, onSuccess: () -> Unit) {
        authManager.userId?.let { id ->
            processNetworkCall(
                action = { profileRepository.createAuthToken(id, code) },
                onSuccess = { onSuccess() },
                onError = { onError(appContext.getString(R.string.error_unknown_later)) },
                callName = "createAuthToken"
            )
        }
    }

    override fun calculateRentCost(startTime: Long, isOld: Boolean, onResult: (Int) -> Unit) {
        val duration = System.currentTimeMillis() - startTime
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

    override fun isFine(startTime: Long) : Boolean {
        val duration = System.currentTimeMillis() - startTime
        val time = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()
        return time > MAX_RIDE_TIME
    }

    override fun isAccountLinked() : Boolean {
        val profile = profileRepository.getData().profile
        return !profile?.tariff?.lkp.isNullOrEmpty() || !profile?.oldTariff?.lkp.isNullOrEmpty()
    }

    override fun tariffIsActive() : Boolean {
        val profile = profileRepository.getData().profile
        val tariffState = profile?.oldTariffState()
        return tariffState == TariffState.Disabled || tariffState == TariffState.Current
    }

    private fun Profile.oldTariffState(): TariffState {
        return when {
            balance < 0 -> TariffState.Disabled
            oldTariffId.isBlank() && oldTariffEnd == null -> TariffState.Unbilled
            oldTariffEnd != null && oldTariffEnd.time < Date().time -> TariffState.Expired
            else -> TariffState.Current
        }
    }

    companion object {
        private const val MAX_RIDE_TIME = 2880
        private const val FINE_AMOUNT = 30000
    }
}