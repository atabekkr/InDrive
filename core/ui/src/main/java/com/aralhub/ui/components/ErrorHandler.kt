package com.aralhub.ui.components

import android.widget.Toast

interface ErrorHandler {
    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT)
}