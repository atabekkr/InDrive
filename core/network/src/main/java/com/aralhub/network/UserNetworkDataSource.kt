package com.aralhub.network

import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.models.auth.NetworkAuthToken
import com.aralhub.network.models.client.NetworkClient
import com.aralhub.network.requests.auth.NetworkUserAuthRequest
import com.aralhub.network.requests.profile.NetworkUserProfileRequest
import com.aralhub.network.requests.refresh.NetworkRefreshTokenRequest
import com.aralhub.network.requests.verify.NetworkVerifyRequest
import java.io.File

interface UserNetworkDataSource {
    suspend fun userAuth(networkUserAuthRequest: NetworkUserAuthRequest): NetworkResult<ServerResponseEmpty>
    suspend fun userVerify(networkUserVerifyRequest: NetworkVerifyRequest): NetworkResult<NetworkAuthToken>
    suspend fun userProfile(networkUserProfileRequest: NetworkUserProfileRequest): NetworkResult<ServerResponseEmpty>
    suspend fun getUserMe(): NetworkResult<NetworkClient>
    suspend fun userLogout(refreshToken: String): NetworkResult<ServerResponseEmpty>
    suspend fun userPhoto(file: File): NetworkResult<ServerResponseEmpty>
    suspend fun deleteUserProfile(): NetworkResult<Unit>
    suspend fun userRefresh(data: NetworkRefreshTokenRequest): NetworkResult<NetworkAuthToken>
}