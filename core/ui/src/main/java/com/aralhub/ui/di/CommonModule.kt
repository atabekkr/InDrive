package com.aralhub.ui.di

import android.content.Context
import com.aralhub.ui.components.ErrorHandlerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityComponent::class)
internal object CommonModule {

    @Provides
    fun provideErrorHandler(
        @ApplicationContext context: Context,
    ): com.aralhub.ui.components.ErrorHandler = ErrorHandlerImpl(context)
}