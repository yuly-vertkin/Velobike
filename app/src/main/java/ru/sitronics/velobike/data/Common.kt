package ru.sitronics.velobike.data

import android.content.Context

const val ERROR_UNKNOWN = -1
const val ERROR_NO_NETWORK = 5000

sealed class Result<out T> {
    object Loading: Result<Nothing>()
    data class Success<out T>(val data: T): Result<T>()
    data class Error(val error: Throwable): Result<Nothing>()
}

//open class BaseResponse(val errorCode: Int? = null, val errorTitle: String? = null, val errorMessage: String? = null)

class ErrorResponse(val errorMsg: String?)
class BusinessErrorResponse(val message : String?)

class ResponseException(val errorCode: Int, val errorMessage: String? = null) : Exception()

interface AppContextProvider {
    fun getContext() : Context
}

class AppContextProviderImp(private val appContext: Context) : AppContextProvider {
    override fun getContext() = appContext
}

