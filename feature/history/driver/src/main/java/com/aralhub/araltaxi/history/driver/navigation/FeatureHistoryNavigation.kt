package com.aralhub.araltaxi.history.driver.navigation

import com.aralhub.ui.model.args.ShowRideRouteArg

interface FeatureHistoryNavigation {
    fun goToMapFromHistoryDetails(item: ShowRideRouteArg)
}