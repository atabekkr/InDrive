package com.aralhub.network.models.driver

import com.google.gson.annotations.SerializedName

data class NetworkDriverInfo(
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("photo_url")
    val avatar: String?
)