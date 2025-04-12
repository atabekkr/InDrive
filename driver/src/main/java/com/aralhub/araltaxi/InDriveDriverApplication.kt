package com.aralhub.araltaxi

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.yandex.mapkit.MapKitFactory
import com.yandex.maps.mobile.BuildConfig
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class InDriveDriverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("f1c206ee-1f73-468c-8ba8-ec3ef7a7f69a")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Lingver.init(this, "kaa")
    }
}