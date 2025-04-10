package com.aralhub.indrive.core.data.model.ride

import com.aralhub.indrive.core.data.model.offer.ClientRideLocationsItems
import com.aralhub.indrive.core.data.model.offer.ClientRideLocationsItemsCoordinates
import com.aralhub.network.models.ride.RideHistoryNetwork

data class RideHistory(
    val amount: Int,
    val waitAmount: Int,
    val locations: List<ClientRideLocationsItems>,
    val distance: Double,
    val status: String,
    val createdAt: String
)

fun RideHistoryNetwork.toDomain(): RideHistory =
    with(this) {
        RideHistory(
            amount = amount,
            waitAmount = waitAmount,
            locations = locations.points.map {
                ClientRideLocationsItems(
                    ClientRideLocationsItemsCoordinates(
                        it.coordinates.longitude,
                        it.coordinates.latitude
                    ),
                    it.name
                )
            },
            distance = distance,
            status = status,
            createdAt = createdAt
        )

    }
