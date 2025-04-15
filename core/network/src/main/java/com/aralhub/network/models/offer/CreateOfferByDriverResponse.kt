package com.aralhub.network.models.offer

import com.google.gson.annotations.SerializedName

data class CreateOfferByDriverResponse(
    @SerializedName("expires_at")
    val expiresAt: String
)
