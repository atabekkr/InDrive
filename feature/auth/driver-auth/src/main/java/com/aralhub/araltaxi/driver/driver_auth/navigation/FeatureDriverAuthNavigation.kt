package com.aralhub.araltaxi.driver.driver_auth.navigation

interface FeatureDriverAuthNavigation {
    fun goToAddPhoneNumber()
    fun goToOverviewFromAddSMS()
    fun goToAddSMSCode(phone: String)
    fun goToOverviewFromLogo()
}