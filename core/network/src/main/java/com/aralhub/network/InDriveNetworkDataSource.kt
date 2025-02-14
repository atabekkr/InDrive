package com.aralhub.network

import com.aralhub.network.model.NetworkAuthRequest
import com.aralhub.network.retrofit.InDriveNetworkApi
import com.aralhub.network.utils.NetworkUtils.myUnwrap
import com.aralhub.network.utils.NetworkUtils.safeApiCall
import javax.inject.Inject

class InDriveNetworkDataSource @Inject constructor(private val api: InDriveNetworkApi) {
    suspend fun auth(request: NetworkAuthRequest) = safeApiCall { api.auth(request) }.myUnwrap()
}