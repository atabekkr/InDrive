package com.aralhub.network

import androidx.paging.PagingData
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.models.WebSocketServerResponse
import com.aralhub.network.models.auth.NetworkAuthToken
import com.aralhub.network.models.balance.NetworkBalance
import com.aralhub.network.models.balance.NetworkBalanceInfo
import com.aralhub.network.models.cancel.NetworkCancelCause
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
import com.aralhub.network.models.ride.RideHistoryNetwork
import com.aralhub.network.requests.auth.NetworkDriverAuthRequest
import com.aralhub.network.requests.logout.NetworkLogoutRequest
import com.aralhub.network.requests.verify.NetworkVerifyRequest
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DriverNetworkDataSource {
    suspend fun driverAuth(networkDriverAuthRequest: NetworkDriverAuthRequest): NetworkResult<String>
    suspend fun driverVerify(networkDriverVerifyRequest: NetworkVerifyRequest): NetworkResult<NetworkAuthToken>
    suspend fun getDriverVehicle(): NetworkResult<String>
    suspend fun getDriverInfo(): NetworkResult<NetworkDriverInfo>
    suspend fun getDriverInfoWithVehicle(): NetworkResult<NetworkDriverActive>
    suspend fun driverCard(networkDriverCardRequest: NetworkCard): NetworkResult<Boolean>
    suspend fun getDriverBalance(): NetworkResult<NetworkBalance>
    suspend fun getDriverCard(): NetworkResult<NetworkCard>
    suspend fun driverLogout(networkDriverLogoutRequest: NetworkLogoutRequest): NetworkResult<Boolean>
    suspend fun driverPhoto(file: File): NetworkResult<ServerResponseEmpty>
    suspend fun getDriverBalanceInfo(): NetworkResult<NetworkBalanceInfo>
    suspend fun getActiveOrders(sendLocationRequest: NetworkSendLocationRequestWithoutType): NetworkResult<List<WebSocketServerResponse<NetworkActiveOfferResponse>>>
    suspend fun createOffer(
        rideUUID: String,
        amount: Int
    ): NetworkResult<CreateOfferByDriverResponse>

    suspend fun getActiveRide(): NetworkResult<NetworkActiveRideByDriverResponse?>
    suspend fun cancelRide(rideId: Int, cancelCauseId: Int): NetworkResult<Boolean>
    suspend fun updateRideStatus(rideId: Int, status: String): NetworkResult<NetworkRideCompletedResponse?>
    suspend fun getCancelCauses(): NetworkResult<List<NetworkDriverCancelCause>>
    suspend fun getWaitTime(rideId: Int): NetworkResult<NetworkWaitAmount>
    suspend fun getRideHistory(): Flow<PagingData<RideHistoryNetwork>>
}