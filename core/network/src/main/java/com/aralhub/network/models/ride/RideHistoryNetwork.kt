package com.aralhub.network.models.ride

import com.aralhub.network.models.driver.NetworkDriverActive
import com.aralhub.network.models.offer.Locations
import com.aralhub.network.models.websocketclient.ClientRideResponsePassenger
import com.aralhub.network.models.websocketclient.ClientRideResponsePaymentMethod
import com.google.gson.annotations.SerializedName

data class RideHistoryNetwork(
    val id: Int,
    val amount: Int,
    @SerializedName("waiting_amount")
    val waitAmount: Int,
    val locations: Locations,
    val distance: Double,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
)

data class RideHistoryDetailsNetwork(
    val id: Int,
    val amount: Int,
    @SerializedName("waiting_amount")
    val waitAmount: Int,
    val locations: Locations,
    val distance: Double,
    val status: String,
    val passenger: ClientRideResponsePassenger,
    @SerializedName("payment_method")
    val paymentMethod: ClientRideResponsePaymentMethod,
    @SerializedName("created_at")
    val createdAt: String,
)

data class ClientRideHistoryDetailsNetwork(
    val id: Int,
    val amount: Int,
    @SerializedName("waiting_amount")
    val waitAmount: Int,
    val locations: Locations,
    val distance: Double,
    val status: String,
    val driver: NetworkDriverActive?,
    @SerializedName("payment_method")
    val paymentMethod: ClientRideResponsePaymentMethod,
    @SerializedName("created_at")
    val createdAt: String,
)
