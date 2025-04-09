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

fun List<RideHistoryNetwork>.toDomain(): List<RideHistory> =
    with(this) {
        map { rideHistoryNetwork ->
            RideHistory(
                amount = rideHistoryNetwork.amount,
                waitAmount = rideHistoryNetwork.waitAmount,
                locations = rideHistoryNetwork.locations.points.map {
                    ClientRideLocationsItems(
                        ClientRideLocationsItemsCoordinates(
                            it.coordinates.longitude,
                            it.coordinates.latitude
                        ),
                        it.name
                    )
                },
                distance = rideHistoryNetwork.distance,
                status = rideHistoryNetwork.status,
                createdAt = rideHistoryNetwork.createdAt
            )

        }
         }
