package com.aralhub.araltaxi.request.navigation

import com.aralhub.ui.model.args.SelectedLocations


interface FeatureRequestNavigation {
    fun goToSelectFromLocationFromRequestFragment()
    fun goToSelectToLocationFromRequestFragment()
    fun goToCreateOrderFromRequestFragment(selectedLocations: SelectedLocations)
    fun goToGetOffersFromRequestFragment()
    fun goToProfileFromRequestFragment()
    fun goToSupportFromRequestFragment()
    fun goToSavedPlacesFromRequestFragment()
    fun goToHistoryFromRequestFragment()
    fun goToChangeLanguageFromRequestFragment()
    fun goToLogoFromRequestFragment()
    fun goToRideFragmentFromRequestFragment()
}