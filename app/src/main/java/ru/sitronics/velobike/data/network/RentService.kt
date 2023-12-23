package ru.sitronics.velobike.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import ru.sitronics.velobike.data.repositories.auth.LoginDto
import ru.sitronics.velobike.domain.auth.Login

interface RentService {
    @GET("api/rent/rents/not-finished/user")
    suspend fun activeRents(@Body loginDto: LoginDto) : Login
}