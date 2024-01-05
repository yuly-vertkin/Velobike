package ru.sitronics.velobike.domain.profile

import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result

interface ProfileRepository {
    fun getData(): ProfileData
    fun saveData(data: ProfileData)
    fun getProfile() : Flow<Result<Profile>>
}