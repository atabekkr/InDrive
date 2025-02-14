package com.aralhub.network.utils

import com.aralhub.network.NetworkResult
import com.aralhub.network.WrappedResult
import com.aralhub.network.model.ServerResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response

private const val ERROR_MESSAGE_EMPTY_RESPONSE_BODY = "Empty response body"
private const val ERROR_MESSAGE_UNAUTHORISED = "Unauthorized"

private const val ERROR_CODE_CUSTOM_ERROR = 422
private const val ERROR_CODE_UNAUTHORISED = 401

/**
 * Simple wrapper to make request and wrap to NetworkResult
 * */
object NetworkUtils {
    suspend fun <T> makeRequest(apiCall: suspend () -> Response<T>): NetworkResult<T> = try {
        val response = apiCall()
        if (response.isSuccessful && response.body() != null) {
            response.body()?.let {
                val responseBody = it
                NetworkResult.Success(data = responseBody)
            } ?: NetworkResult.Error(ERROR_MESSAGE_EMPTY_RESPONSE_BODY)
        } else {
            val errorBody = response.errorBody()?.string()
            NetworkResult.Error("networkServerError.message")
//            when (response.code()) {
//                ERROR_CODE_CUSTOM_ERROR -> {
//                    errorBody?.let {
////                        val networkServerError = Json.decodeFromString<NetworkServerError>(it)
//                        NetworkResult.Error("networkServerError.message")
//                    } ?: NetworkResult.Error(message = customErrorMessage(response))
//                }
//
//                ERROR_CODE_UNAUTHORISED -> NetworkResult.Error(message = ERROR_MESSAGE_UNAUTHORISED)
//                else -> {
//                    errorBody?.let {
////                        val networkServerError = Json.decodeFromString<NetworkServerMessage>(it)
//                        NetworkResult.Error("networkServerError.message")
//                    }
//                        ?: NetworkResult.Error(message = customErrorMessage(response))
//                }
//            }
        }
    } catch (e: Exception) {
        NetworkResult.Error(message = e.message.toString(), exception = e)
    }

    /** With this extension function, NetworkDataSource no longer dependent on ServerResponse.
     * This also prevents confusing calls like data.data. on ViewModel layer
     * */
    fun <T> NetworkResult<ServerResponse<T>>.unwrap(): NetworkResult<T> { //todo removed generic response data
        return when (this) {
            is NetworkResult.Success -> NetworkResult.Success(data = this.data.data)
            is NetworkResult.Error -> NetworkResult.Error(
                message = this.message,
                exception = this.exception
            )
        }
    }

    fun <T> WrappedResult<T>.myUnwrap(): WrappedResult<T> {
        return when (this) {
            is WrappedResult.Success -> WrappedResult.Success(data = this.data)
            is WrappedResult.Error -> WrappedResult.Error(
                message = this.message
            )

            is WrappedResult.Loading -> WrappedResult.Loading
        }
    }

    private fun <T> customErrorMessage(response: Response<T>) =
        "Error ${response.code()}: ${response.message()}"

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): WrappedResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall.invoke()
                if (response.isSuccessful) {
                    WrappedResult.Success(response.body()!!)
                } else {
                    val except = when (response.code()) {

                        in 400..499 -> {
                            fetchErrorResponseMessage(response.errorBody())
                        }

                        in 500..599 -> {
                            "Server Error!"
                        }

                        else -> {
                            "Http unknown exception"
                        }
                    }
                    WrappedResult.Error(
                        except!!
                    )
                }
            } catch (error: Throwable) {

//                val exception = when (error) {
//
//                    is HttpException -> {
//                        when (error.code()) {
//                            in 400..499 -> {
//                                ClientException(
//                                    message = "$CLIENT_ERROR: ${error.code()}",
//                                    cause = error,
//                                )
//                            }
//
//                            in 500..599 -> ServerException(
//                                message = "${AppConstants.SERVER_ERROR}: ${error.code()}",
//                                cause = error
//                            )
//
//                            else -> UnknownException(
//                                message = "${AppConstants.HTTP_UNKNOWN_ERROR}: ${error.code()}",
//                                cause = error
//                            )
//                        }
//                    }
//
//                    is NoNetworkException -> AppException(
//                        message = AppConstants.CONNECTIVITY_ERROR
//                    )
//
//                    is TimeoutException -> ServerException(
//                        message = AppConstants.TIME_OUT_ERROR
//                    )
//
//                    is ConnectException -> ServerException(
//                        message = AppConstants.TIME_OUT_ERROR
//                    )
//
//                    is SocketTimeoutException -> ServerException(
//                        message = AppConstants.TIME_OUT_ERROR
//                    )
//
//                    is SSLHandshakeException -> ServerException(
//                        message = AppConstants.HANDSHAKE_ERROR
//                    )
//
//                    is UnknownHostException -> NetworkException(
//                        message = AppConstants.UNKNOWN_HOST,
//                        cause = error
//                    )
//
//                    is IOException -> NetworkException(
//                        message = AppConstants.NETWORK_ERROR,
//                        cause = error
//                    )
//
//                    else -> AppException(
//                        message = AppConstants.UNKNOWN_ERROR,
//                        cause = error
//                    )
//
//                }

                WrappedResult.Error("Error")
            }
        }
    }

    private fun fetchErrorResponseMessage(errorBody: ResponseBody?): String? {
        return try {
//            val error = errorBody?.string()?.parse<ErrorResponseData>()
//            error?.message
            "Error occurred"
        } catch (e: Exception) {
            "Неизвестная ошибка"
        }
    }

}