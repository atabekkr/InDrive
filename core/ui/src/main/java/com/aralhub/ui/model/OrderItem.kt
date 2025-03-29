package com.aralhub.ui.model

import android.os.Parcelable
import com.aralhub.ui.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderItem(
    val id: Int,
    val uuid: String,
    val name: String,
    val recommendedPrice: String?,
    val avatar: String,
    val roadPrice: String = "",
    val pickUpDistance: String = "",
    val roadDistance: String = "",
    val paymentType: PaymentType,
    val pickUpAddress: String?,
    val destinationAddress: String? = null,
    val locations: List<ClientRideLocationsUI>
) : Parcelable

@Parcelize
data class ClientRideLocationsUI(
    val coordinates: ClientRideLocationsCoordinatesUI,
    val name: String
): Parcelable

@Parcelize
data class ClientRideLocationsCoordinatesUI(
    val longitude: Double,
    val latitude: Double
): Parcelable

enum class PaymentType(
    val resId: Int,
    val title: String
) {
    CASH(
        resId = R.drawable.ic_cash,
        title = "Naq pul"
    ),
    CARD(
        resId = R.drawable.ic_credit_card_3d,
        title = "Card"
    )
}