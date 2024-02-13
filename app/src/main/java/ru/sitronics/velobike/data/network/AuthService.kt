package ru.sitronics.velobike.data.network

import retrofit2.http.Body
import retrofit2.http.POST
import ru.sitronics.velobike.data.repositories.auth.LoginDto
import ru.sitronics.velobike.data.repositories.auth.RegisterParams
import ru.sitronics.velobike.domain.auth.Register
import ru.sitronics.velobike.domain.auth.UserToken

interface AuthService {
    @POST("api/api-auth/authenticate")
    suspend fun login(@Body loginDto: LoginDto) : UserToken

    @POST(REGISTER_URL)
    suspend fun register(@Body registerParams: RegisterParams): Register
}
