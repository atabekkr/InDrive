package com.aralhub.araltaxi.driver.orders.utils

import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackBar(msg: String) {
    Snackbar.make(
        requireView(),
        msg,
        Snackbar.LENGTH_SHORT
    ).show()
}

fun BottomSheetDialogFragment.showSnackBar(msg: String) {
    val view = dialog?.window?.decorView ?: view
    if (view != null) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
    }
}

fun formatTime(timeLeftMillis: Long): String {
    val secondsLeft = (timeLeftMillis / 1000).toInt()
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    return String.format("%d:%02d", minutes, seconds)
}

fun Fragment.sendPhoneNumberToDial(phone: String = "998913821929") {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = android.net.Uri.parse("tel:$phone")
    startActivity(intent)
}
