package com.aralhub.network.impl

import com.aralhub.network.ClientOffersNetworkDataSource
import com.aralhub.network.api.ClientNetworkApi
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.utils.ex.NetworkEx.safeRequest
import javax.inject.Inject

class ClientOffersNetworkDataSourceImpl @Inject constructor(private val api: ClientNetworkApi) :
    ClientOffersNetworkDataSource {

    override suspend fun acceptOffer(offerId: String): NetworkResult<ServerResponseEmpty> {
       return api.clientAcceptOffer(offerId).safeRequest()
    }

    override suspend fun rejectOffer(offerId: String): NetworkResult<ServerResponseEmpty> {
        return api.clientRejectOffer(offerId).safeRequest()
    }

}