package com.aralhub.indrive.core.data.util

import com.aralhub.indrive.core.data.model.offer.ActiveOfferResponse
import com.aralhub.indrive.core.data.model.offer.OfferAcceptedResponse

sealed class WebSocketEvent {
    data class RideCancel(val rideId: String) : WebSocketEvent()
    data class OfferReject(val rideUUID: String) : WebSocketEvent()
    data class ActiveOffer(val order: ActiveOfferResponse) : WebSocketEvent()
    data class OfferAccepted(val data: OfferAcceptedResponse) : WebSocketEvent()
    data class RideFieldUpdated(val rideId: String, val value: Int) : WebSocketEvent()
    data object RideCancelledByPassenger : WebSocketEvent()
    data object ConnectionFailed: WebSocketEvent()
    data class Unknown(val error: String) : WebSocketEvent()
}
