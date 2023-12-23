package ru.sitronics.velobike.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.sitronics.velobike.data.repositories.content.BikeParams
import ru.sitronics.velobike.data.repositories.content.BikesDto
import ru.sitronics.velobike.data.repositories.content.ParkingDto

interface MapContentService {
    @POST("api/iot/vehicles/search")
    suspend fun getBikes(@Body params: BikeParams) : BikesDto

    @GET("api/supabase/rest/v1/stations?select=%2A")
    suspend fun getParkings(@Query("latitude") latitudeGt: String, @Query("latitude") latitudeLt: String,
                            @Query("longitude") longitudeGt: String, @Query("longitude") longitudeLt: String) : List<ParkingDto>

}