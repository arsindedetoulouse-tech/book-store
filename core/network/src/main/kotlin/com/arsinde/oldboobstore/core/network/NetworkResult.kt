package com.arsinde.oldboobstore.core.network

/**
 * Outcome of a remote call.
 *
 * Transport and serialization failures are mapped to these cases so that no raw
 * Ktor or kotlinx exception ever reaches a caller.
 */
sealed interface NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>

    /**
     * Connectivity-class failure: no connection, timeout, or a non-success
     * status that indicates the server could not serve the request.
     */
    data class ConnectivityFailure(val cause: Throwable? = null) : NetworkResult<Nothing>

    /**
     * The response arrived but could not be interpreted as the expected
     * wire format.
     */
    data class MalformedResponse(val cause: Throwable? = null) : NetworkResult<Nothing>
}

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> =
    when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.ConnectivityFailure -> this
        is NetworkResult.MalformedResponse -> this
    }