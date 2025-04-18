package com.aralhub.network.models.websocketclient

import com.google.gson.annotations.SerializedName

data class ClientRideRequest(
    @SerializedName("passenger_id")
    val passengerId: Int,
    @SerializedName("base_amount")
    val baseAmount: Number,
    @SerializedName("recommended_amount")
    val recommendedAmount: ClientRideRequestRecommendedAmount,
    val locations: ClientRideRequestLocations,
    val comment: String,
    @SerializedName("auto_take")
    val autoTake: Boolean,
    @SerializedName("payment_id")
    val paymentId: Int,
    @SerializedName("option_ids")
    val optionIds: List<Int>,
    @SerializedName("cancel_cause_id")
    val cancelCauseId: Int? = null
)

data class ClientRideRequestRecommendedAmount(
    @SerializedName("min_amount")
    val minAmount: Int,
    @SerializedName("max_amount")
    val maxAmount: Int,
    @SerializedName("recommended_amount")
    val recommendedAmount: Int
)

data class ClientRideRequestLocations(
    val points: List<ClientRideRequestLocationsItems>
)

data class ClientRideRequestLocationsItems(
    val coordinates: ClientRideRequestLocationsItemsCoordinates,
    val name: String
)

data class ClientRideRequestLocationsItemsCoordinates(
    val longitude: Number,
    val latitude: Number
)

data class ClientRideResponse(
    val uuid: String,
    val passenger: ClientRideResponsePassenger,
    @SerializedName("base_amount")
    val baseAmount: Number,
    @SerializedName("updated_amount")
    val updatedAmount: Number? = null,
    @SerializedName("recommended_amount")
    val recommendedAmount: ClientRideResponseRecommendedAmount,
    val locations: ClientRideResponseLocations,
    val comment: String,
    @SerializedName("payment_method")
    val paymentMethod: ClientRideResponsePaymentMethod,
    val options: ClientRideResponseOptions,
    @SerializedName("auto_take")
    val autoTake: Boolean,
    val distance: ClientRideResponseDistance,
    @SerializedName("cancel_cause_id")
    val cancelCauseId: Int? = null
)

data class ClientRideResponseRecommendedAmount(
    @SerializedName("min_amount")
    val minAmount: Number,
    @SerializedName("max_amount")
    val maxAmount: Number,
    @SerializedName("recommended_amount")
    val recommendedAmount: Number
)

data class ClientRideResponseDistance(
    val segments: List<ClientRideResponseDistanceItem>,
    @SerializedName("total_distance")
    val totalDistance: Number,
    @SerializedName("total_duration")
    val totalDuration: Number)

data class ClientRideResponseDistanceItem(
    val distance: Number,
    val duration: Number,
    @SerializedName("start_point")
    val startPoint: ClientRideResponseDistanceItemPoint,
    @SerializedName("end_point")
    val endPoint: ClientRideResponseDistanceItemPoint,
)

data class ClientRideResponseDistanceItemPoint(
    val coordinates: ClientRideResponseDistanceItemStartPointCoordinates,
    val name: String
)

/**
 * @param longitude -180, 180
 * @param latitude -90, 90
 * */
data class ClientRideResponseDistanceItemStartPointCoordinates(
    val longitude: Number,
    val latitude: Number
)

data class ClientRideResponseOptions(
    val options: List<ClientRideResponseOptionsItem>
)

data class ClientRideResponseOptionsItem(
    val id: Int,
    val name: String
)

data class ClientRideResponsePaymentMethod(
    val id: Int,
    val name: String,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class ClientRideResponsePassenger(
    @SerializedName("id")
    val userId: Int,
    @SerializedName("full_name")
    val userFullName: String,
    @SerializedName("rating")
    val userRating: Number,
    @SerializedName("photo")
    val avatar: String?
)

data class ClientRideResponseLocations(
    val points: List<ClientRideResponseLocationsItems>
)

data class ClientRideResponseLocationsItems(
    val coordinates: ClientRideResponseLocationsItemsCoordinates,
    val name: String
)

data class ClientRideResponseLocationsItemsCoordinates(
    val longitude: Number,
    val latitude: Number
)