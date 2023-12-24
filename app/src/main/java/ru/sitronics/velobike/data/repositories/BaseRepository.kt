package ru.sitronics.velobike.data.repositories

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.sitronics.velobike.R
import ru.sitronics.velobike.SHARED_PREFERENCES_NAME
import ru.sitronics.velobike.data.network.isNetworkAvailable
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.ERROR_NO_NETWORK
import ru.sitronics.velobike.data.ErrorResponse
import ru.sitronics.velobike.data.ERROR_UNKNOWN
import ru.sitronics.velobike.data.ResponseException
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.tools.Logg
import java.lang.reflect.Type

open class BaseRepository<T>(
    appContextProvider: AppContextProvider,
    private val gson: Gson
) {
    protected val context = appContextProvider.getContext()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Volatile
    private var cache: T? = null

    open fun getData() : T? = cache

    open fun saveData(data: T?) {
        cache = data
    }

    protected fun <DTO, RESULT> callAction(action: suspend () -> DTO) : Flow<Result<RESULT>> = flow {
        emit(Result.Loading)
        try {
// for testing
            delay(5000)

            val resDto = action()
            val result = when {
                resDto is ResponseDto<*> -> resDto.toModel()
                resDto is List<*> && resDto.firstOrNull() is ResponseDto<*> ->
                    resDto.map { (it as ResponseDto<*>).toModel() }
                else -> resDto
            } ?: throw ResponseException(ERROR_UNKNOWN, context.getString(R.string.error_unknown))

            emit(Result.Success((result as RESULT)))
        } catch (e: Exception) {
            val exception = when {
                !isNetworkAvailable(context) -> ResponseException(ERROR_NO_NETWORK, context.getString(R.string.error_no_network))
                e is HttpException -> {
                    val error = try {
                        val errorStr = e.response()?.errorBody()?.string()
                        gson.fromJson(errorStr, ErrorResponse::class.java)
                    } catch (_: Exception) {
                        ErrorResponse(context.getString(R.string.error_unknown))
                    }
                    ResponseException(ERROR_UNKNOWN, error.errorMsg)
                }
                else -> e
            }

//            if (!isNetworkAvailable(context))
//                ResponseException(ERROR_NO_NETWORK, "No network")
//            else
//
//            val errorStr = (e as HttpException).response()?.errorBody()?.string()
//            val error = gson.fromJson(errorStr, ErrorResponse::class.java)
//            val exc = ResponseException(NO_ERROR, error.errorMsg)

            Logg.e(exception)
            emit(Result.Error(exception))
        }
    }

    protected suspend fun <DTO, RESULT> callSupabaseAction(action: suspend () -> DTO) : Flow<Result<RESULT>> =
        withContext(Dispatchers.IO) {
            callAction(action)
        }

    protected fun getBooleanPreference(key: String) =
        sharedPreferences.getBoolean(key, false)

    protected fun setBooleanPreference(key: String, value: Boolean?) {
        value?.let {
            with(sharedPreferences.edit()) {
                putBoolean(key, it)
                apply()
            }
        }
    }

    protected fun getIntPreference(key: String) =
        sharedPreferences.getInt(key, 0)

    protected fun setIntPreference(key: String, value: Int) {
        with(sharedPreferences.edit()) {
            putInt(key, value)
            apply()
        }
    }

    protected fun getStringPreference(key: String) =
        sharedPreferences.getString(key, null)

    protected fun setStringPreference(key: String, value: String?) {
        value?.let {
            with(sharedPreferences.edit()) {
                putString(key, it)
                apply()
            }
        }
    }

    protected fun getStringSetPreference(key: String): Set<String>? =
        sharedPreferences.getStringSet(key, emptySet())

    protected fun setStringSetPreference(key: String, value: Set<String>?) {
        value?.let {
            with(sharedPreferences.edit()) {
                putStringSet(key, it)
                apply()
            }
        }
    }

    protected fun getStringListPreference(key: String) : List<String>? {
        val json: String? = sharedPreferences.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        return try { gson.fromJson(json, type) }
        catch (e: Exception) { null }
    }

    protected fun setStringListPreference(key: String, value: List<String>?) {
        with (sharedPreferences.edit()) {
            try {
                putString(key, gson.toJson(value))
                apply()
            } catch (e: Exception) {}
        }
    }

    protected fun <T> readFromAsset(name: String, classOfT: Class<T>) : T? {
        return try {
            gson.fromJson(context.assets.open(name).reader(), classOfT)
        } catch (ex: Exception) {
            null
        }
    }

    // temp for form asset file
    protected fun <T> tempWriteAsset(value: T) : String {
        return try {
            gson.toJson(value)
        } catch (ex: Exception) {
            ""
        }
    }

    companion object {
/*
        private const val USER_ID_KEY = "USER_ID_KEY"

        fun getUserIdPreference(context: Context) =
            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(USER_ID_KEY, null)

        fun clearOldUserPreferences(context: Context, value: String?) {
            val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                putString(USER_ID_KEY, value)
                apply()
            }
        }
*/
    }
}