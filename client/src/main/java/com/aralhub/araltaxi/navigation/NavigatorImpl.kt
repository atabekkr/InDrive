package com.aralhub.araltaxi.navigation

import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import com.aralhub.araltaxi.client.R
import com.aralhub.araltaxi.create_order.CreateOrderFragment
import com.aralhub.araltaxi.create_order.navigation.FeatureCreateOrderNavigation
import com.aralhub.araltaxi.history.client.navigation.FeatureHistoryNavigation
import com.aralhub.araltaxi.profile.client.navigation.FeatureProfileNavigation
import com.aralhub.araltaxi.request.navigation.FeatureRequestNavigation
import com.aralhub.araltaxi.ride.navigation.sheet.FeatureRideNavigation
import com.aralhub.araltaxi.savedplaces.editsavedplace.EditSavedPlaceFragment
import com.aralhub.araltaxi.savedplaces.navigation.FeatureSavedPlaceNavigation
import com.aralhub.araltaxi.savedplaces.saveaddress.SaveAddressFragment
import com.aralhub.araltaxi.select_location.SelectLocationFragment
import com.aralhub.client.clientauth.addsms.AddSMSFragment
import com.aralhub.client.clientauth.navigation.FeatureClientAuthNavigation
import com.aralhub.offers.navigation.FeatureOffersNavigation
import com.aralhub.ui.model.args.SelectedLocations
import com.aralhub.ui.model.args.ShowRideRouteArg
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorImpl @Inject constructor() : Navigator, FeatureClientAuthNavigation,
    FeatureRequestNavigation, FeatureOffersNavigation, FeatureProfileNavigation,
    FeatureSavedPlaceNavigation, FeatureCreateOrderNavigation, FeatureRideNavigation,
    FeatureHistoryNavigation {

    private var navController: NavController? = null

    override fun bind(navController: NavController) {
        this.navController = navController
    }

    override fun unbind() {
        this.navController = null
    }

    override fun goToAddPhoneNumber() {
        navController?.navigate(R.id.action_logoFragment_to_addPhoneFragment)
    }

    override fun goToRequestFromAddName() {
        navController?.navigate(R.id.action_addNameFragment_to_requestFragment)
    }

    override fun goToRequestFromVerify() {
        navController?.navigate(R.id.action_addSmsFragment_to_requestFragment)
    }

    override fun goToAddSMSCode(phone: String) {
        navController?.navigate(
            R.id.action_addPhoneFragment_to_addSmsFragment,
            AddSMSFragment.args(phone)
        )
    }

    override fun goToAddName() {
        navController?.navigate(R.id.action_addSmsFragment_to_addNameFragment)
    }

    override fun goToRequestFromLogo() {
        navController?.navigate(R.id.action_logoFragment_to_requestFragment)
    }

    override fun goToSelectFromLocationFromRequestFragment() {
        navController?.navigate(
            R.id.action_requestFragment_to_selectLocationFragment,
            SelectLocationFragment.args(owner = SelectLocationFragment.Companion.LocationOwner.FROM)
        )
    }

    override fun goToSelectToLocationFromRequestFragment() {
        navController?.navigate(
            R.id.action_requestFragment_to_selectLocationFragment,
            SelectLocationFragment.args(owner = SelectLocationFragment.Companion.LocationOwner.TO)
        )
    }

    override fun goToCreateOrderFromRequestFragment(selectedLocations: SelectedLocations) {
        navController?.navigate(
            R.id.action_requestFragment_to_createOrderFragment,
            CreateOrderFragment.args(selectedLocations)
        )
    }

    override fun goToGetOffersFromRequestFragment() {
        navController?.navigate(R.id.action_requestFragment_to_offersFragment)
    }

    override fun goToProfileFromRequestFragment() {
        navController?.navigate(R.id.action_requestFragment_to_profileFragment)
    }

    override fun goToSupportFromRequestFragment() {
        navController?.navigate(R.id.action_requestFragment_to_supportFragment)
    }

    override fun goToSavedPlacesFromRequestFragment() {
        navController?.navigate(R.id.action_requestFragment_to_savedPlacesFragment)
    }

    override fun goToHistoryFromRequestFragment() {
        navController?.navigate(R.id.action_requestFragment_to_historyFragment)
    }

    override fun goToChangeLanguageFromRequestFragment() {
        navController?.navigate(R.id.action_requestFragment_to_changeLanguageFragment)
    }

    override fun goToLogoFromRequestFragment() {
        navController?.navigate(R.id.action_requestFragment_to_logoFragment)
    }

    override fun goToRideFragmentFromRequestFragment() {
        navController?.navigate(R.id.action_requestFragment_to_rideFragment)
    }

    override fun goToRideFragment() {
        navController?.navigate(R.id.action_offersFragment_to_rideFragment)
    }

    override fun goBackToRequestFragmentFromOffersFragment() {
        navController?.navigateUp()
    }

    override fun goToLogoFragmentFromProfileFragment() {
        navController?.navigate(R.id.action_profileFragment_to_logoFragment)
    }

    override fun navigateToEditSavedPlace(savedPlaceId: Int) {
        navController?.navigate(
            R.id.action_savedPlacesFragment_to_editSavedPlaceFragment,
            EditSavedPlaceFragment.args(savedPlaceId)
        )
    }

    override fun navigateToSelectLocationFromSavedPlaces() {
        navController?.navigate(
            R.id.action_savedPlacesFragment_to_selectLocationFragment,
            SelectLocationFragment.args(owner = SelectLocationFragment.Companion.LocationOwner.UNSPECIFIED)
        )
    }

    override fun navigateToSaveAddressFromSavedPlaces(
        name: String,
        address: String,
        latitude: Double,
        longitude: Double
    ) {
        navController?.navigate(
            R.id.action_savedPlacesFragment_to_saveAddressFragment,
            SaveAddressFragment.args(name, address, latitude, longitude)
        )
    }

    override fun goToOffersFromCreateOrderFragment() {
        navController?.navigate(R.id.action_createOrderFragment_to_offersFragment)
    }

    override fun goToSelectFromLocationFromCreateOrderFragment() {
        navController?.navigate(
            R.id.action_createOrderFragment_to_selectLocationFragment,
            SelectLocationFragment.args(SelectLocationFragment.Companion.LocationOwner.FROM)
        )
    }

    override fun goToSelectToLocationFromCreateOrderFragment() {
        navController?.navigate(
            R.id.action_createOrderFragment_to_selectLocationFragment,
            SelectLocationFragment.args(SelectLocationFragment.Companion.LocationOwner.TO)
        )
    }


    override fun goBackToCreateOfferFromRide() {
        Log.i("RateDriver", "called: $navController")
        navController?.navigateUp()
    }

    override fun goToMapFromHistoryDetails(item: ShowRideRouteArg) {
        val bundle = Bundle()
        bundle.putParcelable("HistoryDetail", item)
        navController?.navigate(
            R.id.action_historyFragment_to_showOrderRouteFragment,
            bundle
        )
    }
}