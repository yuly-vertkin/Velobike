package ru.sitronics.velobike.data.repositories.auth

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.network.AuthService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.domain.auth.AuthData
import ru.sitronics.velobike.domain.auth.AuthRepository
import ru.sitronics.velobike.domain.auth.RegisterData
import ru.sitronics.velobike.domain.auth.UserToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImp @Inject constructor(
    private val service: AuthService,
    appContextProvider: AppContextProvider,
    gson: Gson,
) : BaseRepository<AuthData>(appContextProvider, gson), AuthRepository {

    override fun getData() : AuthData =
        super.getData() ?: AuthData(
            login = getStringPreference(LOGIN_KEY),
            password = getStringPreference(PASSWORD_KEY)
        )

    override fun saveData(data: AuthData) {
        super.saveData(data)
        setStringPreference(LOGIN_KEY, data.login)
        setStringPreference(PASSWORD_KEY, data.password)
    }

    override fun login(login: String, password: String) : Flow<Result<UserToken>> =
        callAction { service.login(LoginDto(login, password)) }

    override fun register(registerData: RegisterData) : Flow<Result<String>> =
        callAction { service.register(RegisterParams(registerData)) }

    companion object {
        private const val LOGIN_KEY = "LOGIN_KEY"
        private const val PASSWORD_KEY = "PASSWORD_KEY"
    }
}
