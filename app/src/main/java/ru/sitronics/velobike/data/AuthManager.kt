package ru.sitronics.velobike.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.sitronics.velobike.SHARED_PREFERENCES_NAME
import ru.sitronics.velobike.tools.Logg
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    var token: String? = null
        get() = field ?: getStringPreference(AUTH_TOKEN_KEY)
        set(value) {
            field = value/*?.let {
                val b64payload = it.split(".")[1]
                String(Base64Utils.decode(b64payload))
            }*/
            Logg.d("!!! $field")
            setStringPreference(AUTH_TOKEN_KEY, field)
        }

    val isLogged: Boolean
        get() = token != null

/*
    fun getAuthToken() : String? =
        token ?: getStringPreference(AUTH_TOKEN_KEY)

    fun saveAuthToken(rawToken: String) {
        val b64payload = rawToken.split(".")[1]
        token = String(Base64Utils.decode(b64payload))
        Log.d("!!! $token")

        setStringPreference(AUTH_TOKEN_KEY, token)
    }
*/

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
    }
}