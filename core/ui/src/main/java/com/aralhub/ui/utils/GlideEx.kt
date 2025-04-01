package com.aralhub.ui.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey

object GlideEx {
    fun displayAvatar(url : String, imageView: ImageView) {
        Glide.with(imageView.context)
            .load("https://araltaxi.aralhub.uz/$url")
            .centerCrop()
            .placeholder(com.aralhub.ui.R.drawable.ic_user)
            .signature(ObjectKey(System.currentTimeMillis()))
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    }
}