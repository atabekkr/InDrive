package com.aralhub.indrive.core.data.repository

import com.aralhub.network.InDriveNetworkDataSource
import com.aralhub.network.model.NetworkAuthRequest
import com.aralhub.network.model.NetworkAuthResponseData
import com.aralhub.network.utils.WrappedResult

class SampleRepository(
    private val networkDataSource: InDriveNetworkDataSource,
) {
    suspend fun auth(request: NetworkAuthRequest): WrappedResult<NetworkAuthResponseData> {
        return networkDataSource.auth(request)
    }
}