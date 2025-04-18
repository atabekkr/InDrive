package com.aralhub.araltaxi.driver.orders.navigation

import com.aralhub.ui.model.args.ShowRideRouteArg

interface FeatureOrdersNavigation {
    fun goToProfileFromOrders()
    fun goToSupportFromOrders()
    fun goToHistoryFromOrders()
    fun goToChangeLanguageFromOrders()
    fun goToRevenueFromOrders()
    fun goToLogoFromOrders()
    fun goToMapFromOrders(item: ShowRideRouteArg)
}