package com.aralhub.network.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.aralhub.network.ClientNetworkDataSource
import com.aralhub.network.api.ClientNetworkApi
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.models.driver.NetworkDriverCard
import com.aralhub.network.models.location.NetworkLocationPoint
import com.aralhub.network.models.price.NetworkRecommendedPrice
import com.aralhub.network.models.price.NetworkStandardPrice
import com.aralhub.network.models.ride.ClientRideHistoryDetailsNetwork
import com.aralhub.network.models.ride.NetworkRideActive
import com.aralhub.network.models.ride.NetworkRideSearch
import com.aralhub.network.models.ride.NetworkWaitAmount
import com.aralhub.network.models.ride.RideHistoryDetailsNetwork
import com.aralhub.network.models.ride.RideHistoryNetwork
import com.aralhub.network.requests.price.NetworkRecommendedRidePriceRequest
import com.aralhub.network.requests.ride.NetworkClientRideRequest
import com.aralhub.network.utils.ClientHistoryPagingSource
import com.aralhub.network.utils.ex.NetworkEx.safeRequest
import com.aralhub.network.utils.ex.NetworkEx.safeRequestServerResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClientNetworkDataSourceImpl @Inject constructor(private val api: ClientNetworkApi) :
    ClientNetworkDataSource {

    override suspend fun cancelRide(
        rideId: Int,
        cancelCauseId: Int
    ): NetworkResult<ServerResponseEmpty> {
        return api.cancelRide(rideId, cancelCauseId).safeRequest()
    }

    override suspend fun cancelRideByPassenger(rideId: Int): NetworkResult<ServerResponseEmpty> {
        return api.cancelRideByPassenger(rideId).safeRequest()
    }

    override suspend fun getActiveRideByPassenger(userId: Int): NetworkResult<NetworkRideActive> {
        return api.getActiveRideByPassenger(userId).safeRequestServerResponse()
    }

    override suspend fun getSearchRideByPassengerId(userId: Int): NetworkResult<NetworkRideSearch> {
        return api.getSearchRideByPassengerId(userId).safeRequestServerResponse()
    }

    override suspend fun updateSearchRideAmount(
        rideId: String,
        amount: Number
    ): NetworkResult<ServerResponseEmpty> {
        return api.updateSearchRideAmount(rideId, amount).safeRequest()
    }

    override suspend fun getRecommendedPrice(points: List<NetworkLocationPoint>): NetworkResult<NetworkRecommendedPrice> {
        return api.getRidePrice(NetworkRecommendedRidePriceRequest(points))
            .safeRequestServerResponse()
    }

    override suspend fun clientRide(networkClientRideRequest: NetworkClientRideRequest): NetworkResult<NetworkRideSearch> {
        return api.clientRide(networkClientRideRequest).safeRequestServerResponse()
    }

    override suspend fun clientCancelSearchRide(rideId: String): NetworkResult<ServerResponseEmpty> {
        return api.clientCancelSearchRide(rideId).safeRequest()
    }

    override suspend fun updateAutoTake(
        rideId: String,
        autoTake: Boolean
    ): NetworkResult<ServerResponseEmpty> {
        return api.updateAutoTake(rideId, autoTake).safeRequest()
    }

    override suspend fun getWaitTime(rideId: Int): NetworkResult<NetworkWaitAmount> {
        return api.getWaitAmount(rideId).safeRequestServerResponse()
    }

    override suspend fun getStandard(): NetworkResult<NetworkStandardPrice> {
        return api.getStandardPrice().safeRequest()
    }

    override suspend fun getDriverCard(driverId: Int): NetworkResult<NetworkDriverCard> {
        return api.getCardInfo(driverId).safeRequestServerResponse()
    }

    val NETWORK_PAGE_SIZE = 5
    override suspend fun getRideHistory(): Flow<PagingData<RideHistoryNetwork>> {
        return Pager(config = PagingConfig(
            pageSize = NETWORK_PAGE_SIZE,
            enablePlaceholders = false
        ),
            pagingSourceFactory = { ClientHistoryPagingSource(api) }).flow
    }

    override suspend fun getHistoryRideDetails(rideId: Int): NetworkResult<ClientRideHistoryDetailsNetwork> {
        return api.getHistoryRideDetails(rideId).safeRequestServerResponse()
    }
}