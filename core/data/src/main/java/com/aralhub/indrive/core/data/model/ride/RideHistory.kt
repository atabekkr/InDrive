package com.aralhub.indrive.core.data.model.ride

import com.aralhub.indrive.core.data.model.offer.ClientRideLocationsItems
import com.aralhub.indrive.core.data.model.offer.ClientRideLocationsItemsCoordinates
import com.aralhub.network.models.ride.ClientRideHistoryDetailsNetwork
import com.aralhub.network.models.ride.RideHistoryDetailsNetwork
import com.aralhub.network.models.ride.RideHistoryNetwork

data class RideHistory(
    val rideId: Int,
    val ridePrice: Int,
    val locations: List<ClientRideLocationsItems>,
    val distance: Double,
    val status: String,
    val userName: String,
    val createdAt: String,
    val paymentMethodId: Int
)

fun RideHistoryNetwork.toDomain(): RideHistory =
    with(this) {
        RideHistory(
            rideId = id,
            ridePrice = amount + waitAmount,
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
            userName = "",
            createdAt = createdAt,
            paymentMethodId = 1
        )

    }

fun RideHistoryDetailsNetwork.toDomain(): RideHistory =
    with(this) {
        RideHistory(
            rideId = id,
            ridePrice = amount + waitAmount,
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
            userName = passenger.userFullName,
            createdAt = createdAt,
            paymentMethodId = paymentMethod.id
        )

    }


data class ClientRideHistory(
    val rideId: Int,
    val ridePrice: Int,
    val locations: List<ClientRideLocationsItems>,
    val distance: Double,
    val status: String,
    val driverName: String,
    val carName: String,
    val createdAt: String,
    val paymentMethodId: Int
)

fun ClientRideHistoryDetailsNetwork.toDomain(): ClientRideHistory =
    with(this) {
        ClientRideHistory(
            rideId = id,
            ridePrice = amount + waitAmount,
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
            driverName = driver?.fullName ?: "",
            carName = driver?.vehicleType?.kk ?: "",
            createdAt = createdAt,
            paymentMethodId = paymentMethod.id
        )

    }