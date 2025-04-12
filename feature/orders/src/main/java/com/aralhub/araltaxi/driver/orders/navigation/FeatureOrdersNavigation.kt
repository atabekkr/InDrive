package com.aralhub.araltaxi.driver.orders.navigation

import com.aralhub.ui.model.OrderItem

interface FeatureOrdersNavigation {
    fun goToProfileFromOrders()
    fun goToSupportFromOrders()
    fun goToHistoryFromOrders()
    fun goToChangeLanguageFromOrders()
    fun goToRevenueFromOrders()
    fun goToLogoFromOrders()
    fun goToMapFromOrders(order: OrderItem)
}