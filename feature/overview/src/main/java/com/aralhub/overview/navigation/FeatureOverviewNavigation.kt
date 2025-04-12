package com.aralhub.overview.navigation

import android.os.Bundle

interface FeatureOverviewNavigation {
    fun goToAcceptOrders(argument: Bundle? = null)
    fun goToProfileFromOverview()
    fun goToSupportFromOverview()
    fun goToHistoryFromOverview()
    fun goToChangeLanguageFromOverview()
    fun goToRevenueFromOverview()
    fun goToLogoFromOverview()
}