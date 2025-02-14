package com.aralhub.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) :
        NetworkResult<Nothing>()
}

sealed interface WrappedResult<out T> {
    data class Success<T>(val data: T) : WrappedResult<T>
    data class Error(val message: String) : WrappedResult<Nothing>
    data object Loading : WrappedResult<Nothing>
}

/**
 * Error model of server's response error to display error message to user in UI (now only 422 error send this model, otherwise just a NetworkMessage)
 *
 * */
//@Serializable
data class NetworkServerError(
    val message: String,
    val errors: Map<String, List<String>>,
)

//@Serializable
data class NetworkServerMessage(
    val message: String,
)


