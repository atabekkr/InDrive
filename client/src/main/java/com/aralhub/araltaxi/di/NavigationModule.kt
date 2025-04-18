package com.aralhub.araltaxi.di

import com.aralhub.araltaxi.create_order.navigation.FeatureCreateOrderNavigation
import com.aralhub.araltaxi.history.client.navigation.FeatureHistoryNavigation
import com.aralhub.client.clientauth.navigation.FeatureClientAuthNavigation
import com.aralhub.araltaxi.navigation.Navigator
import com.aralhub.araltaxi.navigation.NavigatorImpl
import com.aralhub.araltaxi.profile.client.navigation.FeatureProfileNavigation
import com.aralhub.araltaxi.request.navigation.FeatureRequestNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.araltaxi.savedplaces.navigation.FeatureSavedPlaceNavigation
import com.aralhub.offers.navigation.FeatureOffersNavigation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun bindFeatureAuthNavigation(navigatorImpl: NavigatorImpl): FeatureClientAuthNavigation

    @Binds
    abstract fun bindFeatureRequestNavigation(navigatorImpl: NavigatorImpl): FeatureRequestNavigation

    @Binds
    abstract fun bindFeatureOffersNavigation(navigatorImpl: NavigatorImpl): FeatureOffersNavigation

    @Binds
    abstract fun bindFeatureProfileNavigation(navigatorImpl: NavigatorImpl): FeatureProfileNavigation

    @Binds
    abstract fun bindFeatureSavedPlaceNavigation(navigatorImpl: NavigatorImpl): FeatureSavedPlaceNavigation

    @Binds
    abstract fun bindFeatureCreateOrderNavigation(navigatorImpl: NavigatorImpl): FeatureCreateOrderNavigation

    @Binds
    abstract fun bindFeatureRideNavigation(navigatorImpl: NavigatorImpl): FeatureRideNavigation

    @Binds
    abstract fun bindFeatureHistoryNavigation(navigatorImpl: NavigatorImpl): FeatureHistoryNavigation

    @Binds
    abstract fun bindNavigator(navigatorImpl: NavigatorImpl): Navigator
}