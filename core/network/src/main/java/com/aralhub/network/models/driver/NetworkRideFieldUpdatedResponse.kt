package com.aralhub.network.models.driver

import com.google.gson.annotations.SerializedName

data class NetworkRideFieldUpdatedResponse(
    @SerializedName("ride_id")
    val rideId: String,
    val value: Int
)
