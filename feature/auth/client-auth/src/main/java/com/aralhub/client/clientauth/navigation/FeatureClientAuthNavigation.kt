package com.aralhub.client.clientauth.navigation

interface FeatureClientAuthNavigation {
    fun goToAddPhoneNumber()
    fun goToRequestFromAddName()
    fun goToRequestFromVerify()
    fun goToAddSMSCode(phone: String)
    fun goToAddName()
    fun goToRequestFromLogo()
}