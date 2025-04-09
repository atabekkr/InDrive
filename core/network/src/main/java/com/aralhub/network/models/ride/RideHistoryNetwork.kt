package com.aralhub.network.models.ride

import com.aralhub.network.models.offer.Locations
import com.google.gson.annotations.SerializedName

data class RideHistoryNetwork(
    val amount: Int,
    @SerializedName("waiting_amount")
    val waitAmount: Int,
    val locations: Locations,
    val distance: Double,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String
)
