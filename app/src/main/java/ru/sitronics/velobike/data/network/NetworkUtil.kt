package ru.sitronics.velobike.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import ru.sitronics.velobike.BuildConfig
import ru.sitronics.velobike.domain.auth.AuthManager
import ru.sitronics.velobike.tools.Md5Utils
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.util.Date

class SecureInterceptor(private val authManager: AuthManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val timestamp = (Date().time / 1000).toString()
        val saltForHash = salt + request.url + timestamp

        val hash = Md5Utils.md5(saltForHash)

        val headers = request.headers.newBuilder()
            .add(VERSION_KEY, BuildConfig.VERSION_NAME)
            .add(HASH_KEY, hash)
            .add(TIMESTAMP_KEY, timestamp)
            .add(USER_AGENT_KEY, getUserAgentInfo())
            .build()

        request = request.newBuilder().headers(headers).build()

        return chain.proceed(request)
    }

    private fun getUserAgentInfo(): String {
        return "${Build.MODEL};Android;${Build.VERSION.SDK_INT};${BuildConfig.VERSION_NAME};${authManager.userId}"
    }

    companion object {
        private val salt = Md5Utils.md5(BuildConfig.QRATOR_SECRET + BuildConfig.VERSION_NAME)
        private val SOURCE_KEY = "source"
        private val SOURCE_VAL = "mobile_app"
        private val VERSION_KEY = "App-Version"
        private val HASH_KEY = "Hash"
        private val TIMESTAMP_KEY = "Timestamp"
        private val USER_AGENT_KEY = "User-Agent"
    }
}

class AuthInterceptor(private val authManager: AuthManager): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val token = if (request.url.toString().startsWith(BuildConfig.BASE_URL))
            authManager.accessToken else authManager.accessTokenOldApi

        if (token != null) {
            val headers = request.headers
                .newBuilder()
                .add(AUTH_HEADER_KEY, AUTH_HEADER_VAL + token)
                .build()
            request = request.newBuilder().headers(headers).build()
        }

        val response = chain.proceed(request)

        if (response.code == HTTP_UNAUTHORIZED) {
            authManager.needReLogin()
        }

        return response
    }

    companion object {
        private const val AUTH_HEADER_KEY = "Authorization"
        private const val AUTH_HEADER_VAL = "Bearer "
    }
}

class TestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response = chain.proceed(request)
        val isS = response.isSuccessful
//  check: response.body?.string()
        return response
    }
}

object DebugOkHttpHelper {
        private const val MAX_CONTENT_SIZE = 10000
        private const val LOG_TAG = "OkHttp"

        private val LOGGING = HttpLoggingInterceptor {
            val message = if (it.length < MAX_CONTENT_SIZE) {
                it
            } else {
                "${it.take(MAX_CONTENT_SIZE)} *** ${it.length - MAX_CONTENT_SIZE} BYTES REDUCED ***"
            }
            logOkHttpClient(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        fun getInterceptor(): Interceptor {
            return LOGGING
        }

        private fun logOkHttpClient(message: String) {
            Timber.tag(LOG_TAG).d(message)
//            Platform.get().log(message)
        }
}

// TODO: может не работать на не стандартных девайсах,
//  поэтому используется в качестве проверки только в случае получения HTTP error
fun isNetworkAvailable(context: Context): Boolean {
    // register activity with the connectivity manager service
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // if the android version is equal to M or greater we need to use the NetworkCapabilities to check what type of network has the internet connection
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // Returns a Network object corresponding to the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return false
        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport or Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    } else { // if the android version is below M
        @Suppress("DEPRECATION") val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION") return networkInfo.isConnected
    }
}