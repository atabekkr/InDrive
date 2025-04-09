package com.aralhub.araltaxi.history.driver

import com.aralhub.indrive.core.data.model.ride.RideHistory
import com.aralhub.ui.model.ClientRideLocationsCoordinatesUI
import com.aralhub.ui.model.ClientRideLocationsUI
import com.aralhub.ui.model.RideHistoryUI

fun List<RideHistory>.asUI(): List<RideHistoryUI> {
    return this.map {
        RideHistoryUI(
            amount = it.amount,
            waitAmount = it.waitAmount,
            locations = it.locations.map {
                ClientRideLocationsUI(
                    name = it.name,
                    coordinates = ClientRideLocationsCoordinatesUI(
                        it.coordinates.latitude.toDouble(),
                        it.coordinates.longitude.toDouble()
                    )
                )
            },
            distance = it.distance,
            status = it.status,
            createdAt = it.createdAt
        )
    }
}