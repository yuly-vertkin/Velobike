package ru.sitronics.velobike

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import com.yandex.maps.mobile.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class VelobikeApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Yandex MapKit
        MapKitFactory.setApiKey("ae3586ac-f9c6-45b8-87e3-08c514d8f59e");

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}