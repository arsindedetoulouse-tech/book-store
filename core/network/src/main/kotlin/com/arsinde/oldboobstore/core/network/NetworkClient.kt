package com.arsinde.oldboobstore.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Builds the project's [HttpClient].
 *
 * Engine, timeouts, content negotiation and logging live here so a consuming
 * module never configures a client of its own.
 */
fun createNetworkHttpClient(config: NetworkConfig): HttpClient = HttpClient(OkHttp) {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
    }

    install(Logging) {
        level = LogLevel.INFO
        sanitizeHeader { header -> header == "Authorization" }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = config.requestTimeoutMillis
        connectTimeoutMillis = config.connectTimeoutMillis
        socketTimeoutMillis = config.socketTimeoutMillis
    }
}

/**
 * Runs [block], mapping transport and serialization failures onto
 * [NetworkResult]. Cancellation is rethrown so structured concurrency survives.
 */
internal suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> T,
): NetworkResult<T> = try {
    NetworkResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: JsonConvertException) {
    // Ktor wraps a kotlinx failure in its own exception type.
    NetworkResult.MalformedResponse(e)
} catch (e: SerializationException) {
    NetworkResult.MalformedResponse(e)
} catch (e: Throwable) {
    NetworkResult.ConnectivityFailure(e)
}