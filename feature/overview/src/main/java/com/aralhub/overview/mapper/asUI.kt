package com.aralhub.overview.mapper

import com.aralhub.indrive.core.data.model.offer.ActiveOfferResponse
import com.aralhub.ui.model.ClientRideLocationsCoordinatesUI
import com.aralhub.ui.model.ClientRideLocationsUI
import com.aralhub.ui.model.OrderItem
import com.aralhub.ui.model.PaymentType

fun ActiveOfferResponse.asUI() = with(this) {
    val paymentType = if (paymentType == 1) PaymentType.CASH else PaymentType.CARD
    val pickUpDistanceValue =
        if (pickUpDistance < 1000) "${pickUpDistance.toInt()} m" else "${pickUpDistance.toInt() / 1000} km"
    val roadDistanceValue =
        if (roadDistance < 1000) "${roadDistance.toInt()} m" else "${roadDistance.toInt() / 1000} km"
    OrderItem(
        id = id,
        uuid = uuid,
        name = name,
        recommendedPrice = recommendedPrice,
        avatar = avatar,
        roadPrice = roadPrice,
        pickUpDistance = pickUpDistanceValue,
        roadDistance = roadDistanceValue,
        paymentType = paymentType,
        pickUpAddress = pickUpAddress,
        destinationAddress = destinationAddress,
        locations = locations.map {
            ClientRideLocationsUI(
                ClientRideLocationsCoordinatesUI(
                    it.coordinates.longitude.toDouble(),
                    it.coordinates.latitude.toDouble()
                ), it.name
            )
        },
        comment = comment,
        hasLuggage = hasLuggage
    )
}