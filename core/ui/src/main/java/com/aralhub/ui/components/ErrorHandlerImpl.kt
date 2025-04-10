package com.aralhub.ui.components

import android.content.Context
import android.widget.Toast

//internal class ErrorHandlerImpl @Inject constructor(
//    @ApplicationContext private val context: Context,
//    private val activity: Activity
//) : ErrorHandler {
//    private var isToastShowing = false
//    override fun showToast(message: String, duration: Int) {
//        Handler(Looper.getMainLooper()).post {
//            val toast = Toast.makeText(context, message, duration)
//            if (isToastShowing) {
//                toast.cancel()
//                toast.show()
//            } else {
//                toast.show()
//            }
//
//        }
//    }
//}

class ErrorHandlerImpl(private val context: Context) : ErrorHandler {
    override fun showToast(message: String, duration: Int) {
        Toast.makeText(context, message, duration).show()
    }
}