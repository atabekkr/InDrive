package com.aralhub.network.impl

import com.aralhub.network.UserNetworkDataSource
import com.aralhub.network.api.UserNetworkApi
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.models.auth.NetworkAuthToken
import com.aralhub.network.models.client.NetworkClient
import com.aralhub.network.requests.auth.NetworkUserAuthRequest
import com.aralhub.network.requests.logout.NetworkLogoutRequest
import com.aralhub.network.requests.profile.NetworkUserProfileRequest
import com.aralhub.network.requests.refresh.NetworkRefreshTokenRequest
import com.aralhub.network.requests.verify.NetworkVerifyRequest
import com.aralhub.network.utils.ex.MultipartEx
import com.aralhub.network.utils.ex.NetworkEx.safeRequest
import com.aralhub.network.utils.ex.NetworkEx.safeRequestEmpty
import com.aralhub.network.utils.ex.NetworkEx.safeRequestServerResponse
import java.io.File
import javax.inject.Inject

class UserNetworkDataSourceImpl @Inject constructor(private val api: UserNetworkApi) : UserNetworkDataSource {
    override suspend fun userAuth(networkUserAuthRequest: NetworkUserAuthRequest): NetworkResult<ServerResponseEmpty> {
        return api.userAuth(networkUserAuthRequest).safeRequest()
    }

    override suspend fun userVerify(networkUserVerifyRequest: NetworkVerifyRequest): NetworkResult<NetworkAuthToken> {
        return api.userVerify(networkUserVerifyRequest).safeRequestServerResponse()
    }

    override suspend fun userProfile(networkUserProfileRequest: NetworkUserProfileRequest): NetworkResult<ServerResponseEmpty> {
        return api.userProfile(networkUserProfileRequest).safeRequest()
    }

    override suspend fun getUserMe(): NetworkResult<NetworkClient> {
        return api.getUserMe().safeRequestServerResponse()
    }

    override suspend fun userRefresh(data: NetworkRefreshTokenRequest): NetworkResult<NetworkAuthToken> {
        return api.userRefresh(data).safeRequestServerResponse()
    }

    override suspend fun userLogout(refreshToken: String): NetworkResult<ServerResponseEmpty> {
        return api.userLogout(NetworkLogoutRequest(refreshToken)).safeRequest()
    }

    override suspend fun userPhoto(file: File): NetworkResult<ServerResponseEmpty> {
        return api.userPhoto(MultipartEx.getMultipartFromFile(file)).safeRequest()
    }

    override suspend fun deleteUserProfile(): NetworkResult<ServerResponseEmpty> {
        return api.deleteUserProfile().safeRequest()
    }
}