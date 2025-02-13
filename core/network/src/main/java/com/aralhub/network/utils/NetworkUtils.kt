package com.aralhub.network.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Simple wrapper to make request and wrap to NetworkResult
 * */
object NetworkUtils {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): WrappedResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall.invoke()
                if (response.isSuccessful) {
                    WrappedResult.Success(response.body()!!)
                } else {
                    WrappedResult.Error(java.lang.Exception(response.message()))
                }
            } catch (error: Throwable) {

                WrappedResult.Error(java.lang.Exception("Error!"))
            }
        }
    }
}

sealed interface WrappedResult<out T> {
    data class Success<T>(val data: T) : WrappedResult<T>
    data class Error(val exception: Exception) : WrappedResult<Nothing>
    data class Loading(val status: Boolean = true) : WrappedResult<Nothing>
}