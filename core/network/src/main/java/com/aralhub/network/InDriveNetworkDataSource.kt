package com.aralhub.network

import com.aralhub.network.model.NetworkAuthRequest
import com.aralhub.network.model.NetworkAuthResponseData
import com.aralhub.network.retrofit.InDriveNetworkApi
import com.aralhub.network.utils.NetworkUtils.safeApiCall
import com.aralhub.network.utils.WrappedResult

class InDriveNetworkDataSource(private val api: InDriveNetworkApi) {
    suspend fun auth(request: NetworkAuthRequest): WrappedResult<NetworkAuthResponseData> {
        return safeApiCall {
            api.auth(request)
        }
    }
}