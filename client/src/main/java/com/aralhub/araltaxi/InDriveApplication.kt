package com.aralhub.araltaxi

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.yandex.mapkit.MapKitFactory
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class InDriveApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val currentLocale = Locale.getDefault()

        // Временно меняем на русский перед инициализацией MapKit
        Locale.setDefault(Locale("ru"))
        val config = this.resources.configuration
        config.setLocale(Locale("ru"))
        this.resources.updateConfiguration(config, this.resources.displayMetrics)

        MapKitFactory.setApiKey("f1c206ee-1f73-468c-8ba8-ec3ef7a7f69a")
        MapKitFactory.initialize(this)

        // Возвращаем обратно текущий язык приложения (узбекский)
        Locale.setDefault(currentLocale)
        config.setLocale(currentLocale)
        this.resources.updateConfiguration(config, this.resources.displayMetrics)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Lingver.init(this, "kaa")
    }
}