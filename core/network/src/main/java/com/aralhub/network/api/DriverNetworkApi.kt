package com.aralhub.network.api

import com.aralhub.network.models.ServerResponse
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.models.WebSocketServerResponse
import com.aralhub.network.models.auth.NetworkAuthToken
import com.aralhub.network.models.balance.NetworkBalance
import com.aralhub.network.models.balance.NetworkBalanceInfo
import com.aralhub.network.models.cancel.NetworkDriverCancelCause
import com.aralhub.network.models.card.NetworkCard
import com.aralhub.network.models.driver.NetworkActiveRideByDriverResponse
import com.aralhub.network.models.driver.NetworkDriverActive
import com.aralhub.network.models.driver.NetworkDriverInfo
import com.aralhub.network.models.driver.NetworkRideCompletedResponse
import com.aralhub.network.models.location.NetworkSendLocationRequestWithoutType
import com.aralhub.network.models.offer.CreateOfferByDriverResponse
import com.aralhub.network.models.offer.NetworkActiveOfferResponse
import com.aralhub.network.models.ride.NetworkWaitAmount
import com.aralhub.network.models.ride.RideHistoryDetailsNetwork
import com.aralhub.network.models.ride.RideHistoryNetwork
import com.aralhub.network.requests.auth.NetworkDriverAuthRequest
import com.aralhub.network.requests.logout.NetworkLogoutRequest
import com.aralhub.network.requests.verify.NetworkVerifyRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface DriverNetworkApi {

    @POST("/driver/auth")
    suspend fun driverAuth(@Body networkDriverAuthRequest: NetworkDriverAuthRequest): Response<ServerResponse<String>>

    @POST("/driver/verify/")
    suspend fun driverVerify(@Body networkDriverVerifyRequest: NetworkVerifyRequest): Response<ServerResponse<NetworkAuthToken>>

    @GET("/driver/vehicle")
    suspend fun getDriverVehicle(): Response<ServerResponse<String>>

    @GET("/driver/info")
    suspend fun getDriverInfo(): Response<ServerResponse<NetworkDriverInfo>>

    @GET("/driver/info_with_vehicle")
    suspend fun getDriverInfoWithVehicle(): Response<ServerResponse<NetworkDriverActive>>

    @GET("/driver/card")
    suspend fun getDriverCard(): Response<ServerResponse<NetworkCard>>

    @POST("/driver/card")
    suspend fun driverCard(@Body networkDriverCardRequest: NetworkCard): Response<ServerResponseEmpty>

    @GET("/driver/balance")
    suspend fun getDriverBalance(): Response<ServerResponse<NetworkBalance>>

    @POST("/driver/logout/")
    suspend fun driverLogout(@Body networkDriverRequest: NetworkLogoutRequest): Response<ServerResponseEmpty>

    @Multipart
    @POST("/driver/photo")
    suspend fun driverPhoto(@Part photo: MultipartBody.Part): Response<ServerResponseEmpty>

    @POST("/websocket/ride/get_rides")
    suspend fun getActiveOrders(@Body sendLocationRequest: NetworkSendLocationRequestWithoutType): Response<ServerResponse<List<WebSocketServerResponse<NetworkActiveOfferResponse>>>>

    @GET("/driver/balance_info")
    suspend fun getDriverBalanceInfo(): Response<ServerResponse<NetworkBalanceInfo>>

    @POST("/websocket/ride/{ride_uuid}/offer")
    suspend fun createOffer(
        @Path("ride_uuid") rideUUID: String,
        @Query("amount") amount: Int
    ): Response<ServerResponse<CreateOfferByDriverResponse>>

    @GET("/websocket/get_active_ride_by_driver")
    suspend fun getActiveRideByDriver(): Response<ServerResponse<NetworkActiveRideByDriverResponse?>>

    @PUT("/ride/{rideId}/cancel_ride_by_driver")
    suspend fun cancelRide(
        @Path("rideId") rideId: Int,
        @Query("cancel_cause_id") cancelCauseId: Int
    ): Response<ServerResponseEmpty>

    @PUT("/ride/{rideId}/update_ride")
    suspend fun updateRideStatus(
        @Path("rideId") rideId: Int,
        @Query("status") status: String
    ): Response<ServerResponse<NetworkRideCompletedResponse?>>

    @GET("/cancel_cause/by_type?type=by_driver")
    suspend fun getCancelCauses(): Response<ServerResponse<List<NetworkDriverCancelCause>>>

    @GET("/ride/{ride_id}/get_wait_amount")
    suspend fun getWaitAmount(@Path("ride_id") rideId: Int): Response<ServerResponse<NetworkWaitAmount>>

    @GET("/ride/get_rides_history")
    suspend fun getDriverRideHistory(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): Response<ServerResponse<List<RideHistoryNetwork>>>

    @GET("/ride/get_ride_info/{ride_id}")
    suspend fun getHistoryRideDetails(
        @Path("ride_id") rideId: Int
    ): Response<ServerResponse<RideHistoryDetailsNetwork>>

}