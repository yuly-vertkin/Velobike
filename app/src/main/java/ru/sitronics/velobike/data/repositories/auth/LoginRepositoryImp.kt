package ru.sitronics.velobike.data.repositories.auth

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import ru.sitronics.velobike.data.network.LoginService
import ru.sitronics.velobike.data.repositories.BaseRepository
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.domain.auth.LoginData
import ru.sitronics.velobike.domain.auth.LoginRepository
import ru.sitronics.velobike.domain.auth.Login
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepositoryImp @Inject constructor(
    private val service: LoginService,
    appContextProvider: AppContextProvider,
    gson: Gson,
) : BaseRepository<LoginData>(appContextProvider, gson), LoginRepository {

    override fun getData() : LoginData =
        super.getData() ?: LoginData(
            login = getStringPreference(LOGIN_KEY),
            password = getStringPreference(PASSWORD_KEY)
        )

    override fun saveData(data: LoginData) {
        super.saveData(data)
        setStringPreference(LOGIN_KEY, data.login)
        setStringPreference(PASSWORD_KEY, data.password)
    }

    override fun login(login: String, password: String) : Flow<Result<Login>> =
        callAction { service.login(LoginDto(login, password)) }

    companion object {
        private const val LOGIN_KEY = "LOGIN_KEY"
        private const val PASSWORD_KEY = "PASSWORD_KEY"
    }
}
