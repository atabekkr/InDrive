package com.aralhub.araltaxi.driver.orders.utils

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackBar(msg: String) {
    Snackbar.make(
        requireView(),
        msg,
        Snackbar.LENGTH_SHORT
    ).show()
}

fun formatTime(timeLeftMillis: Long): String {
    val secondsLeft = (timeLeftMillis / 1000).toInt()
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    return String.format("%d:%02d", minutes, seconds)
}
