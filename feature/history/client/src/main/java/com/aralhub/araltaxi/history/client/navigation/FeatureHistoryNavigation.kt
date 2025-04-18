package com.aralhub.araltaxi.history.client.navigation

import com.aralhub.ui.model.args.ShowRideRouteArg

interface FeatureHistoryNavigation {
    fun goToMapFromHistoryDetails(item: ShowRideRouteArg)
}