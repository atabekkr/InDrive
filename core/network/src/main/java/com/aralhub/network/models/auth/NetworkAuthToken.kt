package com.aralhub.network.models.auth

import com.google.gson.annotations.SerializedName

data class NetworkAuthToken(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("is_signed_up")
    val isSignedUp: Boolean
)
