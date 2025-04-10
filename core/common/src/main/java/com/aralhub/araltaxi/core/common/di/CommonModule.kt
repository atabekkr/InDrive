package com.aralhub.araltaxi.core.common.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
internal object CommonModule {

//    @Provides
//    fun provideErrorHandler(
//        @ApplicationContext context: Context,
//        activity: Activity
//    ): com.aralhub.ui.components.ErrorHandler = com.aralhub.ui.components.ErrorHandlerImpl(context, activity)
}