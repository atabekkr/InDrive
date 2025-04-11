package com.aralhub.araltaxi.core.common.sharedpreference

import android.content.SharedPreferences

class DriverSharedPreference(private val preference: SharedPreferences) {

    var distance by IntPreference(preference, 3000)

    var access by StringPreference(preference)

    var refresh by StringPreference(preference)

    var phoneNumber by StringPreference(preference)
    var userName by StringPreference(preference)
    var avatar by StringPreference(preference)

    var isLogin by BooleanPreference(preference, false)

    var appVersion by StringPreference(preference)

    fun clear() {
        preference.edit().clear().apply()
    }

}