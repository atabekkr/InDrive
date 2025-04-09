package com.aralhub.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RideHistoryUI(
    val amount: Int,
    val waitAmount: Int,
    val locations: List<ClientRideLocationsUI>,
    val distance: Double,
    val status: String,
    val createdAt: String
): Parcelable

