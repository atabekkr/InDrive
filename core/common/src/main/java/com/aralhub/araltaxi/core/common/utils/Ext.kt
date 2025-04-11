package com.aralhub.araltaxi.core.common.utils

import android.content.Context
import android.location.LocationManager

fun loadJsonFromAssets(context: Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}

fun isGPSEnabled(mContext: Context): Boolean {
    val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}