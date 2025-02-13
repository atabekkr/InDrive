package com.aralhub.indrivedriver.navigation

import androidx.navigation.NavController

interface Navigator {
    fun bind(navController: NavController)
    fun unbind()
}