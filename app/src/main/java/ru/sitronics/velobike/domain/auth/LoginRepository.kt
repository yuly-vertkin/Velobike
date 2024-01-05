package ru.sitronics.velobike.domain.auth

import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.Result

interface LoginRepository {
    fun getData(): LoginData
    fun saveData(data: LoginData)
    fun login(login: String, password: String) : Flow<Result<UserToken>>
}