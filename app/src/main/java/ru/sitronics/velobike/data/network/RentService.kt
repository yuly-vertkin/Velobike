package ru.sitronics.velobike.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.sitronics.velobike.data.repositories.rent.ActiveRentOldDto
import ru.sitronics.velobike.domain.rent.ActiveRent
import ru.sitronics.velobike.domain.rent.FinishRentParams
import ru.sitronics.velobike.domain.rent.RentStatus
import ru.sitronics.velobike.domain.rent.StartRentParams

interface RentService {
    @POST("api/rent/rents")
    suspend fun startRent(@Body params: StartRentParams) : RentStatus

    @POST("api/rent/rents/{rentId}/finishRent")
    suspend fun finishRent(@Path("rentId") rentId: Int, @Body params: FinishRentParams) : RentStatus

    @GET("api/rent/rents/{rentId}/checkRentStatus")
    suspend fun checkStatus(@Path("rentId") rentId: Int, @Query("deviceId") deviceId: String) : RentStatus

    @GET("api/rent/rents/not-finished/user")
    suspend fun checkActiveRent() : List<ActiveRent>

    @GET("api/supabase/rest/v1/rental_start?select=%2A&bike_type=lte.1")
    suspend fun checkActiveRentOld(@Query("customer_id") uid: String) : List<ActiveRentOldDto>
}