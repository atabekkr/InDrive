package com.aralhub.network.impl

import android.util.Log
import com.aralhub.network.DriverNetworkDataSource
import com.aralhub.network.api.DriverNetworkApi
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
import com.aralhub.network.requests.auth.NetworkDriverAuthRequest
import com.aralhub.network.requests.logout.NetworkLogoutRequest
import com.aralhub.network.requests.verify.NetworkVerifyRequest
import com.aralhub.network.utils.ex.MultipartEx
import com.aralhub.network.utils.ex.NetworkEx.safeRequest
import com.aralhub.network.utils.ex.NetworkEx.safeRequestEmpty
import com.aralhub.network.utils.ex.NetworkEx.safeRequestServerResponse
import com.aralhub.network.utils.ex.NetworkEx.safeRequestServerResponseWithNullData
import java.io.File
import javax.inject.Inject

class DriverNetworkDataSourceImpl @Inject constructor(private val api: DriverNetworkApi) :
    DriverNetworkDataSource {
    override suspend fun driverAuth(networkDriverAuthRequest: NetworkDriverAuthRequest): NetworkResult<String> {
        return api.driverAuth(networkDriverAuthRequest).safeRequestServerResponse()
    }

    override suspend fun driverVerify(networkDriverVerifyRequest: NetworkVerifyRequest): NetworkResult<NetworkAuthToken> {
        return api.driverVerify(networkDriverVerifyRequest).safeRequestServerResponse()
    }

    override suspend fun getDriverVehicle(): NetworkResult<String> {
        return api.getDriverVehicle().safeRequestServerResponse()
    }

    override suspend fun getDriverInfo(): NetworkResult<NetworkDriverInfo> {
        return api.getDriverInfo().safeRequestServerResponse()
    }

    override suspend fun getDriverInfoWithVehicle(): NetworkResult<NetworkDriverActive> {
        return api.getDriverInfoWithVehicle().safeRequestServerResponse()
    }


    override suspend fun driverCard(networkDriverCardRequest: NetworkCard): NetworkResult<Boolean> {
        return api.driverCard(networkDriverCardRequest).safeRequestEmpty()
    }

    override suspend fun getDriverBalance(): NetworkResult<NetworkBalance> {
        return api.getDriverBalance().safeRequestServerResponse()
    }

    override suspend fun getDriverCard(): NetworkResult<NetworkCard> {
        return api.getDriverCard().safeRequestServerResponse()
    }

    override suspend fun driverLogout(networkDriverLogoutRequest: NetworkLogoutRequest): NetworkResult<Boolean> {
        return api.driverLogout(networkDriverLogoutRequest).safeRequestEmpty()
    }

    override suspend fun driverPhoto(file: File): NetworkResult<ServerResponseEmpty> {
        return api.driverPhoto(MultipartEx.getMultipartFromFile(file)).safeRequest()
    }

    override suspend fun getActiveOrders(sendLocationRequest: NetworkSendLocationRequestWithoutType): NetworkResult<List<WebSocketServerResponse<NetworkActiveOfferResponse>>> {
        return api.getActiveOrders(sendLocationRequest).safeRequestServerResponse()
    }

    override suspend fun createOffer(
        rideUUID: String,
        amount: Int
    ): NetworkResult<CreateOfferByDriverResponse?> {
        Log.d("DriverNetworkDataSource", "createOffer: $rideUUID, $amount")
        return api.createOffer(rideUUID, amount).safeRequestServerResponseWithNullData()
    }

    override suspend fun getActiveRide(): NetworkResult<NetworkActiveRideByDriverResponse?> {
        return api.getActiveRideByDriver().safeRequestServerResponse()
    }

    override suspend fun cancelRide(rideId: Int, cancelCauseId: Int): NetworkResult<Boolean> {
        return api.cancelRide(rideId, cancelCauseId).safeRequestEmpty()
    }

    override suspend fun updateRideStatus(rideId: Int, status: String): NetworkResult<NetworkRideCompletedResponse?> {
        return api.updateRideStatus(rideId, status).safeRequestServerResponseWithNullData()
    }

    override suspend fun getDriverBalanceInfo(): NetworkResult<NetworkBalanceInfo> {
        return api.getDriverBalanceInfo().safeRequestServerResponse()
    }

    override suspend fun getCancelCauses(): NetworkResult<List<NetworkDriverCancelCause>> {
        return api.getCancelCauses().safeRequestServerResponse()
    }

    override suspend fun getWaitTime(rideId: Int): NetworkResult<NetworkWaitAmount> {
        return api.getWaitAmount(rideId).safeRequestServerResponse()
    }

}