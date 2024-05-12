package ru.sitronics.velobike.domain.auth

import ru.sitronics.velobike.data.Result

interface AuthRepository {
    fun getData(): AuthData
    fun saveData(data: AuthData)
    suspend fun login(login: String, password: String) : Result<UserToken>
    suspend fun register(registerData: RegisterData) : Result<Register>
}