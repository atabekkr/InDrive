package com.aralhub.network.utils

import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import com.aralhub.araltaxi.core.common.sharedpreference.DriverSharedPreference
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val localStorage: ClientSharedPreference
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        localStorage.access.let { token ->
            request.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}

class DriverAuthInterceptor @Inject constructor(
    private val localStorage: DriverSharedPreference
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        localStorage.access.let { token ->
            request.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}



