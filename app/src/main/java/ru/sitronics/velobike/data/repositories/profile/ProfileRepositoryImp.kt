package ru.sitronics.velobike.data.repositories.profile

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.data.network.ProfileService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.domain.profile.Profile
import ru.sitronics.velobike.domain.profile.ProfileData
import ru.sitronics.velobike.domain.profile.ProfileRepository
import ru.sitronics.velobike.domain.profile.Tariff
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImp @Inject constructor(
    private val service: ProfileService,
    appContextProvider: AppContextProvider,
    gson: Gson,
) : BaseRepository<ProfileData>(appContextProvider, gson), ProfileRepository {

    override fun getData() : ProfileData =
        super.getData() ?: ProfileData()

    override fun saveData(data: ProfileData) {
        super.saveData(data)
    }

    override fun getProfile() : Flow<Result<Profile>> =
        callAction { service.getProfile() }

    override fun getTariffs() : Flow<Result<List<Tariff>>> =
        callAction { service.getTariffs() }

    override fun getTariff(id: String) : Flow<Result<Tariff>> =
        callAction { service.getTariff(id) }
}
