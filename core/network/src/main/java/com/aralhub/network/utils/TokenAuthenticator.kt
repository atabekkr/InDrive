package com.aralhub.network.utils

import android.util.Log
import com.aralhub.network.api.UserNetworkApi
import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import com.aralhub.network.requests.refresh.NetworkRefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val localStorage: ClientSharedPreference,
) : Authenticator {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://araltaxi.aralhub.uz/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authService = retrofit.create(UserNetworkApi::class.java)

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            val responseRefresh = authService.userRefresh(NetworkRefreshTokenRequest(localStorage.refresh))
            Log.i("AccessToken", localStorage.access)
            if (responseRefresh.isSuccessful && responseRefresh.code() == 200) {
                val data = responseRefresh.body()!!
                localStorage.access = data.data.accessToken
                return@runBlocking response.request.newBuilder()
                    .removeHeader("Authorization")
                    .header("Authorization", "Bearer ${localStorage.access}")
                    .build()
            } else null
        }
    }
}