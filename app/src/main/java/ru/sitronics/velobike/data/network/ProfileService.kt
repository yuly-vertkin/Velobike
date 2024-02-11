package ru.sitronics.velobike.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.sitronics.velobike.data.repositories.profile.Cards
import ru.sitronics.velobike.data.repositories.profile.ProfilePackage
import ru.sitronics.velobike.data.repositories.profile.TariffPackage
import ru.sitronics.velobike.domain.profile.Tariff
import ru.sitronics.velobike.domain.profile.TariffPayment
import ru.sitronics.velobike.domain.profile.TariffPaymentParams

interface ProfileService {
    @GET(GET_PROFILE_URL)
    suspend fun getProfile() : ProfilePackage

    @GET(TARIFFS_URL)
    suspend fun getTariffs() : TariffPackage

    @GET(TARIFF_URL)
    suspend fun getTariff(@Path("tariffId") id: String) : Tariff

    @GET(GET_CARDS_URL)
    suspend fun getCards() : Cards

    @POST(PAY_TARIFF_URL)
    suspend fun payTariff(@Body params: TariffPaymentParams): TariffPayment
}