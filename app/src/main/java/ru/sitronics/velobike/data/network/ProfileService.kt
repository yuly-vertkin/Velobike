package ru.sitronics.velobike.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import ru.sitronics.velobike.data.repositories.profile.TariffPackage
import ru.sitronics.velobike.domain.profile.ProfilePackage
import ru.sitronics.velobike.domain.profile.Tariff

interface ProfileService {
    @GET(GET_PROFILE_URL)
    suspend fun getProfile() : ProfilePackage

    @GET(TARIFFS_URL)
    suspend fun getTariffs() : TariffPackage

    @GET(TARIFF_URL)
    suspend fun getTariff(@Path("tariffId") id: String): Tariff
}