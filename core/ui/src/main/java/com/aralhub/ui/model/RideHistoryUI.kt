package com.aralhub.ui.model

import android.os.Parcelable
import com.aralhub.indrive.core.data.model.ride.RideHistory
import kotlinx.parcelize.Parcelize

@Parcelize
data class RideHistoryUI(
    val amount: Int,
    val waitAmount: Int,
    val locations: List<ClientRideLocationsUI>,
    val paymentType: PaymentType,
    val distance: Double,
    val status: String,
    val createdAt: String
) : Parcelable


fun RideHistory.asUI(): RideHistoryUI =
    with(this) {
        RideHistoryUI(
            amount = amount,
            waitAmount = waitAmount,
            locations = locations.map {
                ClientRideLocationsUI(
                    name = it.name,
                    coordinates = ClientRideLocationsCoordinatesUI(
                        it.coordinates.latitude.toDouble(),
                        it.coordinates.longitude.toDouble()
                    )
                )
            },
            distance = distance,
            paymentType = PaymentType.CARD,
            status = status,
            createdAt = createdAt
        )

    }
