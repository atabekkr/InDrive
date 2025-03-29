package com.aralhub.indrive.core.data.model.ride

import com.aralhub.indrive.core.data.model.payment.PaymentMethod
import com.aralhub.indrive.core.data.model.payment.toDomain
import com.aralhub.network.models.option.NetworkOption
import com.aralhub.network.models.ride.NetworkRideActive

fun NetworkRideActive.toDomain() = ActiveRide(
    id = this.id,
    uuid = this.uuid,
    status = this.status,
    amount = this.amount,
    waitAmount = this.waitAmount,
    distance = this.distance,
    isActive = this.isActive,
    createdAt = this.createdAt,
    paymentMethod = this.paymentMethod.toDomain(),
    driver = ActiveRideDriver(
        driverId = this.driver.driverId,
        fullName = this.driver.fullName,
        rating = this.driver.rating.toString(),
        vehicleColor = this.driver.vehicleColor.kk,
        vehicleType = this.driver.vehicleType.kk,
        vehicleNumber = this.driver.plateNumber,
        phoneNumber = this.driver.phoneNumber ?: "",
        photoUrl = this.driver.photoUrl
    ),
    locations = SearchRideLocations(
        points = this.locations.points.map { point ->
            point.toDomain()
        },
    ),
    options = this.options.map { option ->
        option.toDomain()
    },
)

data class ActiveRide(
    val id: Int,
    val uuid: String,
    val status: String,
    val amount: Int,
    val waitAmount: Double,
    val distance: Double,
    val locations: SearchRideLocations,
    val isActive: Boolean,
    val createdAt: String,
    val driver: ActiveRideDriver,
    val paymentMethod: PaymentMethod,
    val options: List<ActiveRideOptions>
)

data class ActiveRideDriver(
    val driverId: Int,
    val fullName: String,
    val rating: String,
    val vehicleColor: String,
    val vehicleType: String,
    val vehicleNumber: String,
    val phoneNumber: String,
    val photoUrl: String?
)

data class ActiveRideVehicleColor(
    val ru: String,
    val en: String,
    val kk: String
)

data class ActiveRideVehicleType(
    val ru: String,
    val en: String,
    val kk: String
)

data class ActiveRidePaymentMethod(
    val id: Int,
    val name: String,
    val isActive: Boolean
)

fun NetworkOption.toDomain() = ActiveRideOptions(
    id = id,
    name = name
)

data class ActiveRideOptions(
    val id: Int,
    val name: String
)