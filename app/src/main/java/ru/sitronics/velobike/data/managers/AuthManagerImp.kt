package ru.sitronics.velobike.data.managers

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.common.util.Base64Utils
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.sitronics.velobike.SHARED_PREFERENCES_NAME
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.domain.auth.UserToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManagerImp @Inject constructor(
    @ApplicationContext context: Context
) : AuthManager {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    override var accessToken: String? = null
        get() = field ?: getStringPreference(AUTH_TOKEN_KEY)
        private set(value) {
            field = value
            setStringPreference(AUTH_TOKEN_KEY, field)
            value?.let { saveOtherData(it) }
        }

    override var accessTokenOldApi: String? = null
        get() = field ?: getStringPreference(AUTH_TOKEN_OLD_KEY)
        private set(value) {
            field = value
            setStringPreference(AUTH_TOKEN_OLD_KEY, field)
        }

    override var userId: String? = null
        get() = field ?: getStringPreference(AUTH_TOKEN_USER_ID_KEY)
        private set(value) {
            field = value
            setStringPreference(AUTH_TOKEN_USER_ID_KEY, field)
        }

    override val isLogged: Boolean
        get() = accessToken != null

    override fun setToken(token: String?) {
        accessToken = token
    }

    private fun saveOtherData(token: String) {
        val gson = GsonBuilder().create()
        val b64payload = token.split(".")[1]
        val jsonString = String(Base64Utils.decode(b64payload))//.replaceFirst("{","{\"token\":$token,")
        val tokens = gson.fromJson(jsonString, UserToken::class.java)
        accessTokenOldApi = tokens.accessTokenOldApi
        userId = tokens.userId
    }

    private fun getStringPreference(key: String) =
        sharedPreferences.getString(key, null)

    private fun setStringPreference(key: String, value: String?) {
        value?.let {
            with(sharedPreferences.edit()) {
                putString(key, it)
                apply()
            }
        }
    }

    companion object {
        private const val AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY"
        private const val AUTH_TOKEN_OLD_KEY = "AUTH_TOKEN_OLD_KEY"
        private const val AUTH_TOKEN_USER_ID_KEY = "AUTH_TOKEN_USER_ID_KEY"
    }
}