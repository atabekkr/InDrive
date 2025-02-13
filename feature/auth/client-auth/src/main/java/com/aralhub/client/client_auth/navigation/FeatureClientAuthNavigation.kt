package com.aralhub.client.client_auth.navigation

interface FeatureClientAuthNavigation {
    fun goToAddPhoneNumber()
    fun goToRequestFromAddName()
    fun goToAddSMSCode(phone: String)
    fun goToAddName()
    fun goToRequestFromLogo()
}