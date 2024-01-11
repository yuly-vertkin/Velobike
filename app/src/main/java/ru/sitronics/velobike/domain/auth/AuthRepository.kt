package ru.sitronics.velobike.domain.auth

import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result

interface AuthRepository {
    fun getData(): AuthData
    fun saveData(data: AuthData)
    fun login(login: String, password: String) : Flow<Result<UserToken>>
    fun register(registerData: RegisterData) : Flow<Result<String>>
}