package ru.sitronics.velobike.tools

import timber.log.Timber

internal object Logg {
    private const val LOG_TAG = "Velobike"

    fun d(message: String?, vararg args: Any?) =
        Timber.tag(LOG_TAG).d(message, args)

    fun e(t: Throwable?) =
        Timber.tag(LOG_TAG).e(t)
}