package com.aralhub.araltaxi.ride.navigation.sheet

import androidx.navigation.NavController
import com.aralhub.araltaxi.client.ride.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SheetNavigatorImpl @Inject constructor() : SheetNavigator,
    FeatureRideBottomSheetNavigation {
    private var navController: NavController? = null

    override fun bind(navController: NavController) {
        this.navController = navController
    }

    override fun unbind() {
        this.navController = null
    }

    override fun goToWaitingForDriver() {
        navController?.navigate(R.id.waitingForDriverBottomSheet)
    }

    override fun goToDriverIsWaiting() {
        navController?.navigate(R.id.driverIsWaitingBottomSheet)
    }

    override fun goToRide() {
        navController?.navigate(R.id.rideBottomSheet)
    }

    override fun goToRideFinished() {
        navController?.navigate(R.id.rideFinishedBottomSheet)
    }

    override fun goToRateDriverFromRideFinished() {
        navController?.navigate(R.id.action_rideFinishedBottomSheet_to_rateDriverBottomSheet)
    }

}