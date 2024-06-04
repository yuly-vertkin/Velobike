package ru.sitronics.velobike.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.sitronics.velobike.data.repositories.profile.Cards
import ru.sitronics.velobike.data.repositories.profile.OtpAuth
import ru.sitronics.velobike.data.repositories.profile.ProfilePackage
import ru.sitronics.velobike.data.repositories.profile.TariffPackage
import ru.sitronics.velobike.domain.profile.LKPStatusData
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
    suspend fun payTariff(@Body params: TariffPaymentParams) : TariffPayment

    @GET("api/mm-integration/client")
    suspend fun getLKPStatus(@Query("clientId") userId: String) : LKPStatusData

    @POST("api/mm-integration/otp")
    suspend fun authInMetro(@Query("clientId") userId: String, @Query("phone") phone: String): OtpAuth

    @POST("api/mm-integration/createToken")
    suspend fun createAuthToken(@Query("clientId") userId: String, @Query("code") code: String): Response<Unit>


}