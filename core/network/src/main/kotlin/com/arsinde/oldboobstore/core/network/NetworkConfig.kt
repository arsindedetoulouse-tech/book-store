package com.arsinde.oldboobstore.core.network

/** Shared client configuration, declared once for the whole project. */
data class NetworkConfig(
    val baseUrl: String,
    val requestTimeoutMillis: Long = 15_000,
    val connectTimeoutMillis: Long = 10_000,
    val socketTimeoutMillis: Long = 15_000,
)