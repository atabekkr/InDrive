package com.aralhub.indrive.core.data.model

import com.aralhub.network.model.NetworkAuthResponseData

data class AuthResponse(
    val success: Boolean,
    val message: String
)

fun NetworkAuthResponseData.asDomain() = AuthResponse(
    this.success,
    this.message
)
