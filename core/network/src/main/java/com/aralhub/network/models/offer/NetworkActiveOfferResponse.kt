package com.aralhub.network.models.offer

import com.aralhub.network.models.websocketclient.ClientRideRequestRecommendedAmount
import com.aralhub.network.models.websocketclient.ClientRideResponseDistance
import com.aralhub.network.models.websocketclient.ClientRideResponseLocationsItems
import com.aralhub.network.models.websocketclient.ClientRideResponseOptions
import com.aralhub.network.models.websocketclient.ClientRideResponsePassenger
import com.aralhub.network.models.websocketclient.ClientRideResponsePaymentMethod
import com.google.gson.annotations.SerializedName

data class NetworkActiveOfferResponse(
    val uuid: String,
    val passenger: ClientRideResponsePassenger,
    @SerializedName("base_amount")
    val baseAmount: Int,
    @SerializedName("updated_amount")
    val updatedAmount: Double?,
    @SerializedName("recommended_amount")
    val recommendedAmount: ClientRideRequestRecommendedAmount,
    @SerializedName("pick_up_address")
    val clientPickUpAddress: String,
    val locations: Locations,
    val comment: String,
    @SerializedName("payment_method")
    val paymentMethod: ClientRideResponsePaymentMethod,
    val options: ClientRideResponseOptions,
    @SerializedName("auto_take")
    val autoTake: Boolean,
    val distance: ClientRideResponseDistance,
    @SerializedName("cancel_cause_id")
    val cancelCauseId: String?
)

data class Locations(
    val points: List<ClientRideResponseLocationsItems>
)


