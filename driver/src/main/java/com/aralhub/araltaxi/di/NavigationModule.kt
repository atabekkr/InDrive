package com.aralhub.araltaxi.di

import com.aralhub.araltaxi.driver.driver_auth.navigation.FeatureDriverAuthNavigation
import com.aralhub.araltaxi.driver.orders.navigation.FeatureOrdersNavigation
import com.aralhub.araltaxi.history.driver.navigation.FeatureHistoryNavigation
import com.aralhub.araltaxi.navigation.Navigator
import com.aralhub.araltaxi.navigation.NavigatorImpl
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
    abstract fun bindFeatureOrdersNavigation(navigatorImpl: NavigatorImpl): FeatureOrdersNavigation

    @Binds
    abstract fun bindHistoryNavigation(navigatorImpl: NavigatorImpl): FeatureHistoryNavigation

    @Binds
    abstract fun bindNavigator(navigatorImpl: NavigatorImpl): Navigator
}