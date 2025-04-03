package com.aralhub.ui.utils

import android.view.View

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