package com.aralhub.network.model

data class ServerResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T,
)