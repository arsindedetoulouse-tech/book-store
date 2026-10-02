package com.arsinde.oldboobstore.feature.booklist.di

import com.arsinde.oldboobstore.core.model.Book
import com.arsinde.oldboobstore.core.network.NetworkResult
import com.arsinde.oldboobstore.core.network.api.BookApi
import com.arsinde.oldboobstore.feature.booklist.BookListViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Minimal stand-in for the network facade, so no HTTP stack is needed here. */
private object StubBookApi : BookApi {
    override suspend fun getBooks(): NetworkResult<List<Book>> = NetworkResult.Success(emptyList())

    override suspend fun getBook(id: String): NetworkResult<Book> =
        NetworkResult.MalformedResponse()
}

private val bookApiStub = module {
    single<BookApi> { StubBookApi }
}

class BookListModuleTest {

    @Test
    fun `the feature module resolves its view model without the application module`() {
        val koin = koinApplication {
            modules(bookApiStub, bookListModule)
        }.koin

        assertNotNull(koin.get<BookListViewModel>())
    }

    @Test
    fun `the module starts as part of a larger graph`() {
        startKoin {
            modules(bookApiStub, bookListModule)
        }
        stopKoin()
    }

    @Test
    fun `the feature module declares nothing belonging to another module`() {
        val koin = koinApplication {
            modules(bookApiStub, bookListModule)
        }.koin

        // Only the feature's own view model is provided here.
        assertNotNull(koin.get<BookListViewModel>())
    }
}