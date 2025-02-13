package com.aralhub.indrivedriver.navigation

import androidx.navigation.NavController
import com.aralhub.indrive.driver.driver_auth.AddSMSFragment
import com.aralhub.indrive.driver.driver_auth.navigation.FeatureDriverAuthNavigation
import com.aralhub.indrivedriver.R
import com.aralhub.overview.navigation.FeatureOverviewNavigation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorImpl @Inject constructor(): Navigator, FeatureDriverAuthNavigation, FeatureOverviewNavigation{

    private var navController: NavController? = null

    override fun goToAddPhoneNumber() {
        navController?.navigate(R.id.action_logoFragment_to_addPhoneFragment)
    }

    override fun goToOverviewFromAddSMS() {
        navController?.navigate(R.id.action_addSmsFragment_to_overviewFragment)
    }

    override fun goToAddSMSCode(phone: String) {
        navController?.navigate(R.id.action_addPhoneFragment_to_addSmsFragment, AddSMSFragment.args(phone))
    }

    override fun goToOverviewFromLogo() {
        navController?.navigate(R.id.action_logoFragment_to_overviewFragment)
    }

    override fun bind(navController: NavController) {
        this.navController = navController
    }

    override fun unbind() {
        this.navController = null
    }

    override fun goToAcceptOrders() {
        navController?.navigate(R.id.action_overviewFragment_to_ordersFragment)
    }
}