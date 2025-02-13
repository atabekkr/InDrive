package com.aralhub.network.retrofit

import com.aralhub.network.model.NetworkAuthRequest
import com.aralhub.network.model.NetworkAuthResponseData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface InDriveNetworkApi {
    @Headers("Accept: application/json")
    @POST(value = "api/v1/auth/send-code")
    suspend fun auth(@Body body: NetworkAuthRequest): Response<NetworkAuthResponseData>

}