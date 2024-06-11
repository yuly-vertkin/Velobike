package ru.sitronics.velobike.data.repositories.profile

import android.content.Context
import com.google.gson.Gson
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.data.network.ProfileService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.domain.profile.Card
import ru.sitronics.velobike.domain.profile.LKPStatusData
import ru.sitronics.velobike.domain.profile.MetroPasswordParameters
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.ProfileData
import ru.sitronics.velobike.domain.profile.ProfileRepository
import ru.sitronics.velobike.domain.profile.Tariff
import ru.sitronics.velobike.domain.profile.TariffPayment
import ru.sitronics.velobike.domain.profile.TariffPaymentParams
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImp @Inject constructor(
    private val service: ProfileService,
    appContext: Context,
    gson: Gson,
) : BaseRepository<ProfileData>(appContext, gson), ProfileRepository {

    override fun getData() : ProfileData =
        super.getData() ?: ProfileData()

    override fun saveData(data: ProfileData) {
        super.saveData(data)
    }

    override suspend fun getProfile() : Result<Profile> =
        callAction { service.getProfile() }

    override suspend fun getTariffs() : Result<List<Tariff>> =
        callAction { service.getTariffs() }

    override suspend fun getTariff(id: String) : Result<Tariff> =
        callAction { service.getTariff(id) }

    override suspend fun getCards(): Result<List<Card>> =
        callAction { service.getCards() }

    override suspend fun payTariff(params: TariffPaymentParams): Result<TariffPayment> =
        callAction { service.payTariff(params) }

    override suspend fun getLKPStatus(userId: String): Result<LKPStatusData> =
        callAction { service.getLKPStatus(userId) }

    override suspend fun authInMetro(userId: String, phone: String) : Result<MetroPasswordParameters> =
        callAction { service.authInMetro(userId, phone) }

    override suspend fun createAuthToken(userId: String, code: String) : Result<Boolean> =
        callAction { service.createAuthToken(userId, code) }


}
