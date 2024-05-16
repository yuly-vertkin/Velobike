package ru.sitronics.velobike.presentation

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.sitronics.velobike.data.AppContextProvider
import ru.sitronics.velobike.data.ERROR_NO_NETWORK
import ru.sitronics.velobike.data.ResponseException
import ru.sitronics.velobike.data.Result
import ru.sitronics.velobike.tools.Logg

abstract class BaseViewModel(appContextProvider: AppContextProvider) : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    protected val context = appContextProvider.getContext()
    private val calledJobs = HashMap<String, Job>()

    // Если force = false Мы не повторяем запрос, если предыдущий еще не отработал
    // Внимание! для корректной работы параллельных запросов необходимо дать им разные имена

    protected fun <T> processNetworkCall(
        action: suspend () -> Result<T>,
        onSuccess: (suspend (T) -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null,
        force: Boolean = false,
        callName: String = DEFAULT_CALL_NAME
    ) {
        val isActive = calledJobs[callName]?.isActive ?: false
        if (isActive && force) {
            calledJobs[callName]?.cancel()
            println("!!! cancel job!")
        }

        if (!isActive || force) {
            calledJobs[callName] = viewModelScope.launch {
                val startTime = System.currentTimeMillis()
                action().let {
                    when (it) {
                        is Result.Success -> {
                            val time = System.currentTimeMillis() - startTime
                            Logg.d("!!! $callName success, time: $time")
                            onSuccess?.invoke(it.data)
                        }
                        is Result.Error -> {
                            if (it.error is ResponseException && it.error.errorCode == ERROR_NO_NETWORK)
                                Logg.d("!!! ${it.error.errorMessage}") //showNoNetworkDialog()
                            else {
                                Logg.d("!!! ERROR $callName ${it.error.message}")
                                onError?.invoke(it.error)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    protected fun <T> processFlowNetworkCall(
        action: () -> Flow<Result<T>>,
        onSuccess: (suspend (T) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        force: Boolean = false,
        callName: String = DEFAULT_CALL_NAME
    ) {
        val isActive = calledJobs[callName]?.isActive ?: false
        if (isActive && force) {
            calledJobs[callName]?.cancel()
            println("!!! cancel job!")
        }

        if (!isActive || force) {
            calledJobs[callName] = viewModelScope.launch {
                action().collect {
//                    isLoading.value = it is Result.Loading || isAnotherActiveNetworkCall(callName)

                    when (it) {
                        is Result.Success -> onSuccess?.invoke(it.data)
                        is Result.Error -> {
                            if (it.error is ResponseException && it.error.errorCode == ERROR_NO_NETWORK)
                                Logg.d("!!! ${it.error.errorMessage}") //showNoNetworkDialog()
                            else
                                onError?.invoke(it.error)
                        }
                        else -> { /* nothing to do */ }
                    }
                }
            }
        }
    }

    protected fun isAnotherActiveNetworkCall(callName: String = DEFAULT_CALL_NAME) =
        calledJobs.any {
            it.key != callName && it.value.isActive
        }

    companion object {
        const val DEFAULT_CALL_NAME = "Call"
    }
}