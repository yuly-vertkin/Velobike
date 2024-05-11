package ru.sitronics.velobike.data.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.sitronics.velobike.data.repositories.rent.ActiveRentOldDto
import ru.sitronics.velobike.domain.rent.ChooseParkingParams
import ru.sitronics.velobike.domain.rent.Feedback
import ru.sitronics.velobike.domain.rent.FeedbackRes
import ru.sitronics.velobike.domain.rent.FinishRentParams
import ru.sitronics.velobike.domain.rent.FinishedRentOld
import ru.sitronics.velobike.domain.rent.Rent
import ru.sitronics.velobike.domain.rent.RentStatus
import ru.sitronics.velobike.domain.rent.StartRentParams

interface RentService {
    @GET("api/rent/rents/{rentId}/checkRentStatus")
    suspend fun checkStatus(@Path("rentId") rentId: String, @Query("frameNumber") frameNumber: String) : RentStatus

    @GET("api/rent/rents/not-finished/user")
    suspend fun checkActiveRent() : List<Rent>

    @GET("api/supabase/rest/v1/rental_start?select=%2A&bike_type=lte.1")
    suspend fun checkActiveRentOld(@Query("customer_id") uid: String) : List<ActiveRentOldDto>

    @GET("api/supabase/rest/v1/rental_end?select=%2A")
    suspend fun checkFinishedRentOld(@Query("customer_id") customerId: String, @Query("rental_start_id") rentId: String) : List<FinishedRentOld>

    @POST("api/rent/rents")
    suspend fun startRent(@Body params: StartRentParams) : RentStatus

    @POST("api/rent/rents/{rentId}/finishRent")
    suspend fun finishRent(@Path("rentId") rentId: String, @Body params: FinishRentParams) : RentStatus

    @Multipart
    @POST("api/rent/files/{rentId}/uploadPhoto")
    suspend fun uploadPhotoRent(@Path("rentId") rentId: String, @Part image: MultipartBody.Part) : Response<Unit>

    @POST("api/rent/rents/{rentId}/finishRentAfterUploadPhoto")
    suspend fun finishRentAfterUploadPhoto(@Path("rentId") rentId: String) : RentStatus

    @POST("api/rent/rents/{rentId}/commands/parkBikeToParking")
    suspend fun chooseParking(@Path("rentId") rentId: String, @Body params: ChooseParkingParams) : RentStatus

    @POST("api/feedback/")
    suspend fun sendFeedback(@Body feedback: Feedback) : FeedbackRes

    @POST("api/rent/rents/{rentId}/returnToActiveRent")
    suspend fun returnToActiveRent(@Path("rentId") rentId: String) : RentStatus

    @POST("api/rent/rents/{rentId}/commands/openOmniLock")
    suspend fun unlockWheel(@Path("rentId") rentId: String) : Response<Unit>
}