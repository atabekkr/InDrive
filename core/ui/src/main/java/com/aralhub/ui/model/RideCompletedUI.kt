package com.aralhub.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RideCompletedUI(
    val amount: Int,
    val waitAmount: Double,
    val totalAmount: Int,
    val commissionAmount: Int,
    val cashbackAmount: Double?,
    val duration: Double,
    val distance: Double,
    val paymentMethodId: PaymentType
) : Parcelable