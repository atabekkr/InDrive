package com.aralhub.araltaxi.core.common.sharedpreference

import android.content.SharedPreferences

class ClientSharedPreference(preference: SharedPreferences) {

    var userId by IntPreference(preference)

    var access by StringPreference(preference)

    var refresh by StringPreference(preference)

    var phoneNumber by StringPreference(preference)

    var isLogin by BooleanPreference(preference, false)

    fun clear() {
        access = ""
        refresh = ""
        phoneNumber = ""
        isLogin = false
    }

}