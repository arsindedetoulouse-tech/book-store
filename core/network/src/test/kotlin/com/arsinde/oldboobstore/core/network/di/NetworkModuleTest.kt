package com.arsinde.oldboobstore.core.network.di

import com.arsinde.oldboobstore.core.network.NetworkConfig
import com.arsinde.oldboobstore.core.network.api.BookApi
import io.ktor.client.HttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class NetworkModuleTest {

    /** Stands in for the composition root, which supplies the base URL. */
    private val configModule = module {
        single { BASE_URL }
    }

    private fun graph(): Koin = koinApplication {
        modules(configModule, networkModule)
    }.koin

    @Test
    fun `every declaration resolves without the application module`() {
        val koin = graph()

        assertNotNull(koin.get<NetworkConfig>())
        assertNotNull(koin.get<HttpClient>())
        assertNotNull(koin.get<BookApi>())
    }

    @Test
    fun `the injected client is a singleton`() {
        val koin = graph()

        assertSame(koin.get<HttpClient>(), koin.get<HttpClient>())
    }

    @Test
    fun `BookApi is built from the configured client`() {
        val koin = graph()

        val api = koin.get<BookApi>()

        assertSame(koin.get<NetworkConfig>().baseUrl, "https://example.test")
        assertNotNull(api)
    }

    private companion object {
        const val BASE_URL = "https://example.test"
    }
}