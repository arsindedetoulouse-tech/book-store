package com.arsinde.oldboobstore.core.network.di

import com.arsinde.oldboobstore.core.network.NetworkConfig
import com.arsinde.oldboobstore.core.network.api.BookApi
import com.arsinde.oldboobstore.core.network.api.KtorBookApi
import com.arsinde.oldboobstore.core.network.createNetworkHttpClient
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Injection declarations owned by `:core:network`.
 *
 * The client is provided here and nowhere else, so no consuming module declares
 * an engine, a timeout or a serialization format of its own.
 */
val networkModule: Module = module {
    // The base URL is supplied by the composition root, so this module stays
    // free of environment-specific values.
    single { NetworkConfig(baseUrl = get()) }
    single<HttpClient> { createNetworkHttpClient(get()) }
    single<BookApi> { KtorBookApi(client = get(), baseUrl = get<NetworkConfig>().baseUrl) }
}