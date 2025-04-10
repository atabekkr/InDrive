package com.aralhub.network.api

import com.aralhub.network.models.ServerResponse
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.models.driver.NetworkDriverCard
import com.aralhub.network.models.price.NetworkRecommendedPrice
import com.aralhub.network.models.price.NetworkStandardPrice
import com.aralhub.network.models.ride.NetworkRideActive
import com.aralhub.network.models.ride.NetworkRideSearch
import com.aralhub.network.models.ride.NetworkWaitAmount
import com.aralhub.network.models.ride.RideHistoryNetwork
import com.aralhub.network.requests.price.NetworkRecommendedRidePriceRequest
import com.aralhub.network.requests.ride.NetworkClientRideRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientNetworkApi {
    @POST("/websocket/ride/create")
    suspend fun clientRide(@Body networkClientRideRequest: NetworkClientRideRequest): Response<ServerResponse<NetworkRideSearch>>

    @PUT("/websocket/ride/update_amount/{ride_id}")
    suspend fun updateSearchRideAmount(
        @Path("ride_id") rideId: String,
        @Query("amount") amount: Number
    ): Response<ServerResponseEmpty>

    @POST("/websocket/offer/{offer_id}/accept")
    suspend fun clientAcceptOffer(@Path("offer_id") offerId: String): Response<ServerResponseEmpty>

    @POST("/websocket/offer/{offer_id}/reject")
    suspend fun clientRejectOffer(@Path("offer_id") offerId: String): Response<ServerResponseEmpty>

    @POST("/websocket/ride/cancel/{ride_id}")
    suspend fun clientCancelSearchRide(@Path("ride_id") rideId: String): Response<ServerResponseEmpty>

    @GET("/websocket/get_active_offer_by_ride_id/{ride_id}")
    suspend fun getActiveOfferByRideId(@Path("ride_id") rideId: String): Response<ServerResponseEmpty>

    @PUT("/ride/{ride_id}/cancel_ride_by_passenger")
    suspend fun cancelRideByPassenger(@Path("ride_id") rideId: Int): Response<ServerResponseEmpty>

    @POST("/ride/{ride_id}/cancel_ride")
    suspend fun cancelRide(@Path("ride_id") rideId: Int, @Query("cancel_cause_id") cancelCauseId: Int): Response<ServerResponseEmpty>

    @PUT("/websocket/ride/update_auto_take/{ride_id}")
    suspend fun updateAutoTake(@Path("ride_id") rideId: String, @Query("auto_take") autoTake: Boolean): Response<ServerResponseEmpty>

    @GET("/websocket/get_active_ride_by_passenger")
    suspend fun getActiveRideByPassenger(@Query("user_id") userId: Int): Response<ServerResponse<NetworkRideActive>>

    @GET("/websocket/ride/get_search_ride_by_passenger_id")
    suspend fun getSearchRideByPassengerId(@Query("user_id") userId: Int): Response<ServerResponse<NetworkRideSearch>>

    @POST("/websocket/get_ride_price")
    suspend fun getRidePrice(@Body getRideRecommendedRidePriceRequest: NetworkRecommendedRidePriceRequest): Response<ServerResponse<NetworkRecommendedPrice>>

    @GET("/ride/{ride_id}/get_wait_amount")
    suspend fun getWaitAmount(@Path("ride_id") rideId: Int): Response<ServerResponse<NetworkWaitAmount>>

    @GET("/ride/standart")
    suspend fun getStandardPrice(): Response<NetworkStandardPrice>

    @GET("/ride/get_card_info/{driver_id}")
    suspend fun getCardInfo(@Path("driver_id") driverId: Int): Response<ServerResponse<NetworkDriverCard>>

    @GET("/ride/get_rides_history/")
    suspend fun getRideHistory(@Query("page") page: Int, @Query("page_size")pageSize: Int): Response<ServerResponse<List<RideHistoryNetwork>>>

}