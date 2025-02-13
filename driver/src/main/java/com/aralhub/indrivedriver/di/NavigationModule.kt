package com.aralhub.indrivedriver.di

import com.aralhub.indrive.driver.driver_auth.navigation.FeatureDriverAuthNavigation
import com.aralhub.indrivedriver.navigation.Navigator
import com.aralhub.indrivedriver.navigation.NavigatorImpl
import com.aralhub.overview.navigation.FeatureOverviewNavigation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun bindFeatureAuthNavigation(navigatorImpl: NavigatorImpl): FeatureDriverAuthNavigation

    @Binds
    abstract fun bindFeatureOverviewNavigation(navigatorImpl: NavigatorImpl): FeatureOverviewNavigation

    @Binds
    abstract fun bindNavigator(navigatorImpl: NavigatorImpl): Navigator
}