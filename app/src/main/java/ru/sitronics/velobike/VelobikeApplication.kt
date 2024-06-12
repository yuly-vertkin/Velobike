package ru.sitronics.velobike

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import com.yandex.maps.mobile.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import ru.sitronics.velobike.domain.chat.ChatManager
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class VelobikeApplication: Application() {
    @Inject
    lateinit var chatManager: ChatManager

    override fun onCreate() {
        super.onCreate()
        // Initialize Yandex MapKit
        MapKitFactory.setApiKey("ae3586ac-f9c6-45b8-87e3-08c514d8f59e")

        chatManager.initialize()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}