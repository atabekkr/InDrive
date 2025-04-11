package com.aralhub.ui.utils

import android.R
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar


fun View.setOnSafeClickListener(delayMillis: Long = 1000, onSafeClick: (View) -> Unit) {
    var lastClickTime = 0L

    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= delayMillis) {
            lastClickTime = currentTime
            onSafeClick(view)
        }
    }
}

fun View.hideKeyboard() {
    ViewCompat.getWindowInsetsController(this)?.hide(WindowInsetsCompat.Type.ime())
}

fun View.showKeyboardAndFocus() {
    ViewCompat.getWindowInsetsController(this)?.show(WindowInsetsCompat.Type.ime())
    requestFocus()
}

fun Fragment.showSnackBar(msg: String) {
//    Snackbar.make(
//        requireView(),
//        msg,
//        Snackbar.LENGTH_SHORT
//    ).show()

    val snackbar = Snackbar.make(this.requireView(), "This is a top Snackbar", Snackbar.LENGTH_SHORT)

    // Move Snackbar to the top
    val snackbarView = snackbar.view
    val params = snackbarView.layoutParams as FrameLayout.LayoutParams
    params.gravity = Gravity.TOP
    snackbarView.layoutParams = params

    snackbar.show()

}

fun displayTopSnackBarFeedback(view: View?, message: String?, context: Context?) {
    val snackbar = Snackbar.make(view!!, message!!, Snackbar.LENGTH_LONG)
    snackbar.setBackgroundTint(ContextCompat.getColor(context!!, R.color.black))
    val snackBarView = snackbar.view
    val params = snackBarView.layoutParams as FrameLayout.LayoutParams
    params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
    snackBarView.layoutParams = params
    snackbar.show()
}

fun BottomSheetDialogFragment.showSnackBar(msg: String) {
    val view = dialog?.window?.decorView ?: view
    if (view != null) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
    }
}