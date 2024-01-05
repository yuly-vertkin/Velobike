package ru.sitronics.velobike.data.network

import retrofit2.http.GET
import ru.sitronics.velobike.domain.profile.ProfilePackage

interface ProfileService {
    @GET(GET_PROFILE_URL)
    suspend fun getProfile() : ProfilePackage
}