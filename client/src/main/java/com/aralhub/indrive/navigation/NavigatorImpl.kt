package com.aralhub.indrive.navigation

import androidx.navigation.NavController
import com.aralhub.client.client_auth.AddSMSFragment
import com.aralhub.client.client_auth.navigation.FeatureClientAuthNavigation
import com.aralhub.indrive.R
import com.aralhub.indrive.request.navigation.FeatureRequestNavigation
import com.aralhub.offers.navigation.FeatureOffersNavigation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorImpl @Inject constructor(): Navigator, FeatureClientAuthNavigation, FeatureRequestNavigation, FeatureOffersNavigation{

    private var navController: NavController? = null

    override fun bind(navController: NavController){
        this.navController = navController
    }

    override fun unbind(){
        this.navController = null
    }

    override fun goToAddPhoneNumber() {
        navController?.navigate(R.id.action_logoFragment_to_addPhoneFragment)
    }

    override fun goToRequestFromAddName() {
        navController?.navigate(R.id.action_addNameFragment_to_requestFragment)
    }

    override fun goToAddSMSCode(phone: String) {
        navController?.navigate(R.id.action_addPhoneFragment_to_addSmsFragment,  AddSMSFragment.args(phone))
    }

    override fun goToAddName() {
        navController?.navigate(R.id.action_addSmsFragment_to_addNameFragment)
    }

    override fun goToRequestFromLogo() {
        navController?.navigate(R.id.action_logoFragment_to_requestFragment)
    }

    override fun goToGetOffersFromSendOrderFragment() {
        navController?.navigate(R.id.action_homeFragment_to_offersFragment)
    }

    override fun goToRideFragment() {
        navController?.navigate(R.id.action_offersFragment_to_rideFragment)
    }
}