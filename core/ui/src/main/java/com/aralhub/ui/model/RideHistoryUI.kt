package com.aralhub.ui.model

import android.os.Parcelable
import com.aralhub.indrive.core.data.model.ride.ClientRideHistory
import com.aralhub.indrive.core.data.model.ride.RideHistory
import kotlinx.parcelize.Parcelize

@Parcelize
data class RideHistoryUI(
    val rideId: Int,
    val ridePrice: Int,
    val locations: List<ClientRideLocationsUI>,
    val paymentType: PaymentType,
    val status: String,
    val userName: String,
    val carName: String = "",
    val createdAt: String
) : Parcelable

fun RideHistory.asUI(): RideHistoryUI =
    with(this) {
        RideHistoryUI(
            rideId = rideId,
            ridePrice = ridePrice,
            locations = locations.map {
                ClientRideLocationsUI(
                    name = it.name,
                    coordinates = ClientRideLocationsCoordinatesUI(
                        it.coordinates.longitude.toDouble(),
                        it.coordinates.latitude.toDouble()
                    )
                )
            },
            paymentType = if (paymentMethodId == 1) PaymentType.CASH else PaymentType.CARD,
            status = status,
            userName = userName,
            createdAt = createdAt
        )

    }


fun ClientRideHistory.asUI(): RideHistoryUI =
    with(this) {
        RideHistoryUI(
            rideId = rideId,
            ridePrice = ridePrice,
            locations = locations.map {
                ClientRideLocationsUI(
                    name = it.name,
                    coordinates = ClientRideLocationsCoordinatesUI(
                        it.coordinates.longitude.toDouble(),
                        it.coordinates.latitude.toDouble()
                    )
                )
            },
            paymentType = if (paymentMethodId == 1) PaymentType.CASH else PaymentType.CARD,
            status = status,
            userName = driverName,
            carName = carName,
            createdAt = createdAt
        )

    }