package com.aralhub.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import com.aralhub.ui.R

class ErrorMessageDialog(context: Context) {
    private val dialog: Dialog = Dialog(context).apply {
        setContentView(R.layout.dialog_error_occured)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private val tvErrorMessage: TextView? = dialog.findViewById(R.id.tv_error_message)
    private val tvDismiss: TextView? = dialog.findViewById(R.id.tv_dismiss)

    fun show(errorMessage: String?, isWebSocketError: Boolean = false) {
        if (!dialog.isShowing) {
            tvErrorMessage?.text = errorMessage

            if (isWebSocketError) tvDismiss?.text = "Reconnect"

            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    fun setOnDismissClicked(onClick: (() -> Unit)?) {
        tvDismiss?.setOnClickListener { onClick?.invoke() }
    }
}