package com.aralhub.network.api

import com.aralhub.network.models.ServerResponse
import com.aralhub.network.models.ServerResponseEmpty
import com.aralhub.network.models.auth.NetworkAuthToken
import com.aralhub.network.models.client.NetworkClient
import com.aralhub.network.requests.auth.NetworkUserAuthRequest
import com.aralhub.network.requests.logout.NetworkLogoutRequest
import com.aralhub.network.requests.profile.NetworkUserProfileRequest
import com.aralhub.network.requests.refresh.NetworkRefreshTokenRequest
import com.aralhub.network.requests.verify.NetworkVerifyRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserNetworkApi {

    @POST("/user/auth")
    suspend fun userAuth(@Body networkUserAuthRequest: NetworkUserAuthRequest): Response<ServerResponseEmpty>

    @POST("/user/verify/")
    suspend fun userVerify(@Body networkUserVerifyRequest: NetworkVerifyRequest): Response<ServerResponse<NetworkAuthToken>>

    @POST("/user/profile/")
    suspend fun userProfile(@Body networkUserProfileRequest: NetworkUserProfileRequest): Response<ServerResponseEmpty>

    @GET("/user/me/")
    suspend fun getUserMe(): Response<ServerResponse<NetworkClient>>

    @POST("/user/token/refresh/")
    suspend fun userRefresh(@Body refreshTokenData: NetworkRefreshTokenRequest): Response<ServerResponse<NetworkAuthToken>>

    @POST("/user/logout/")
    suspend fun userLogout(@Body logoutRequest: NetworkLogoutRequest): Response<ServerResponseEmpty>

    @Multipart
    @POST("/user/profile/photo")
    suspend fun userPhoto(@Part photo: MultipartBody.Part): Response<ServerResponseEmpty>

    @DELETE("/user/delete/profile/")
    suspend fun deleteUserProfile(): Response<ServerResponse<Unit>>
}