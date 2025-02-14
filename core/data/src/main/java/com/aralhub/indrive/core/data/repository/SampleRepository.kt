package com.aralhub.indrive.core.data.repository

import com.aralhub.indrive.core.data.model.AuthResponse
import com.aralhub.indrive.core.data.model.asDomain
import com.aralhub.network.InDriveNetworkDataSource
import com.aralhub.network.WrappedResult
import com.aralhub.network.model.NetworkAuthRequest
import javax.inject.Inject

class SampleRepository @Inject constructor(
    private val networkDataSource: InDriveNetworkDataSource,
) {
    suspend fun auth(request: NetworkAuthRequest): WrappedResult<AuthResponse> {
        networkDataSource.auth(request).let {
            return when (it) {
                is WrappedResult.Error -> WrappedResult.Error(it.message)
                WrappedResult.Loading -> WrappedResult.Loading
                is WrappedResult.Success -> WrappedResult.Success(it.data.asDomain()) //todo search about :: method data::asDomain
            }
        }
    }
}