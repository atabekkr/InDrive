package com.aralhub.network

import androidx.paging.PagingData
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.models.driver.NetworkDriverCard
import com.aralhub.network.models.location.NetworkLocationPoint
import com.aralhub.network.models.price.NetworkRecommendedPrice
import com.aralhub.network.models.price.NetworkStandardPrice
import com.aralhub.network.models.ride.NetworkRideActive
import com.aralhub.network.models.ride.NetworkRideSearch
import com.aralhub.network.models.ride.NetworkWaitAmount
import com.aralhub.network.models.ride.RideHistoryNetwork
import com.aralhub.network.requests.ride.NetworkClientRideRequest
import kotlinx.coroutines.flow.Flow

interface ClientNetworkDataSource {

    suspend fun cancelRide(rideId: Int, cancelCauseId: Int): NetworkResult<ServerResponseEmpty>

    suspend fun cancelRideByPassenger(rideId: Int): NetworkResult<ServerResponseEmpty>

    suspend fun getActiveRideByPassenger(userId: Int): NetworkResult<NetworkRideActive>

    suspend fun getSearchRideByPassengerId(userId: Int): NetworkResult<NetworkRideSearch>

    suspend fun updateSearchRideAmount(rideId: String, amount: Number): NetworkResult<ServerResponseEmpty>

    suspend fun getRecommendedPrice(points: List<NetworkLocationPoint>): NetworkResult<NetworkRecommendedPrice>

    suspend fun clientRide(networkClientRideRequest: NetworkClientRideRequest): NetworkResult<NetworkRideSearch>

    suspend fun clientCancelSearchRide(rideId: String): NetworkResult<ServerResponseEmpty>

    suspend fun updateAutoTake(rideId: String, autoTake: Boolean): NetworkResult<ServerResponseEmpty>

    suspend fun getWaitTime(rideId: Int): NetworkResult<NetworkWaitAmount>

    suspend fun getStandard(): NetworkResult<NetworkStandardPrice>

    suspend fun getDriverCard(driverId: Int): NetworkResult<NetworkDriverCard>

    suspend fun getRideHistory(): Flow<PagingData<RideHistoryNetwork>>
}