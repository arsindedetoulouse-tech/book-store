package com.arsinde.oldboobstore.feature.booklist

import com.arsinde.oldboobstore.core.model.Book
import com.arsinde.oldboobstore.core.network.NetworkResult
import com.arsinde.oldboobstore.core.network.api.BookApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.testWithInternalState

/**
 * Test double for [BookApi].
 *
 * The view model under test is unmodified: [BookApi] being an interface is what
 * makes this possible, which is exactly what the network spec requires.
 */
private class FakeBookApi(
    var result: NetworkResult<List<Book>>,
) : BookApi {

    override suspend fun getBooks(): NetworkResult<List<Book>> = result

    override suspend fun getBook(id: String): NetworkResult<Book> =
        when (val current = result) {
            is NetworkResult.Success ->
                current.data.firstOrNull { it.id == id }
                    ?.let { NetworkResult.Success(it) }
                    ?: NetworkResult.MalformedResponse()

            is NetworkResult.ConnectivityFailure -> NetworkResult.ConnectivityFailure()
            is NetworkResult.MalformedResponse -> NetworkResult.MalformedResponse()
        }
}

private val dune = Book(
    id = "1",
    title = "Dune",
    author = "Frank Herbert",
    coverImageUrl = null,
    description = null,
)

class BookListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `a successful load reduces books into state`() = runTest {
        val viewModel = BookListViewModel(FakeBookApi(NetworkResult.Success(listOf(dune))))

        viewModel.testWithInternalState(this) {
            runOnCreate()

            awaitInternalState().also { assertTrue(it.isLoading) }

            awaitInternalState().also { loaded ->
                assertFalse(loaded.isLoading)
                assertEquals(listOf(dune), loaded.books)
                assertNull(loaded.errorMessage)
            }
        }
    }

    @Test
    fun `a connectivity failure reduces an error and posts a side effect`() = runTest {
        val viewModel = BookListViewModel(FakeBookApi(NetworkResult.ConnectivityFailure()))

        viewModel.testWithInternalState(this) {
            runOnCreate()

            awaitInternalState()
            awaitInternalState().also { failed ->
                assertFalse(failed.isLoading)
                assertEquals(BookListViewModel.CONNECTIVITY_MESSAGE, failed.errorMessage)
            }
            expectSideEffect(BookListSideEffect.ShowError(BookListViewModel.CONNECTIVITY_MESSAGE))
        }
    }

    @Test
    fun `a malformed response is distinguishable from a connectivity failure`() = runTest {
        val viewModel = BookListViewModel(FakeBookApi(NetworkResult.MalformedResponse()))

        viewModel.testWithInternalState(this) {
            runOnCreate()

            awaitInternalState()
            awaitInternalState().also { failed ->
                assertEquals(BookListViewModel.MALFORMED_MESSAGE, failed.errorMessage)
            }
            expectSideEffect(BookListSideEffect.ShowError(BookListViewModel.MALFORMED_MESSAGE))
        }
    }

    @Test
    fun `retry reloads after a failure without the view model being modified`() = runTest {
        val api = FakeBookApi(NetworkResult.ConnectivityFailure())
        val viewModel = BookListViewModel(api)

        viewModel.testWithInternalState(this) {
            runOnCreate()

            awaitInternalState()
            awaitInternalState()
            expectSideEffect(BookListSideEffect.ShowError(BookListViewModel.CONNECTIVITY_MESSAGE))

            api.result = NetworkResult.Success(listOf(dune))
            viewModel.retry()

            awaitInternalState()
            awaitInternalState().also { loaded ->
                assertEquals(listOf(dune), loaded.books)
                assertNull(loaded.errorMessage)
            }
        }
    }
}