package com.aralhub.araltaxi.core.common.di

import android.content.Context
import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClientSharedPreferenceModule {
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): ClientSharedPreference {
        val preference = context.getSharedPreferences("SharedPreference", Context.MODE_PRIVATE)
        return ClientSharedPreference(preference)
    }
}