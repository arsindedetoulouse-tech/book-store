package com.arsinde.oldboobstore.feature.booklist

import androidx.lifecycle.ViewModel
import com.arsinde.oldboobstore.core.model.Book
import com.arsinde.oldboobstore.core.network.NetworkResult
import com.arsinde.oldboobstore.core.network.api.BookApi
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

/**
 * Screen state.
 *
 * Carries domain types only. Its shape is not constrained by any remote payload,
 * which is why `:core:model` declares no serialization annotations.
 */
data class BookListState(
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val errorMessage: String? = null,
)

/** One-time events: messages the screen shows exactly once. */
sealed interface BookListSideEffect {
    data class ShowError(val message: String) : BookListSideEffect
}

/**
 * Book list container.
 *
 * Orbit 12 dropped the Intent type parameter: an intent is any suspend function
 * on the container, which [loadBooks] and [retry] are.
 */
class BookListViewModel(
    private val bookApi: BookApi,
) : ViewModel(), OrbitContainerHost<BookListState, BookListState, BookListSideEffect> {

    override val container = orbitContainer<BookListState, BookListSideEffect>(
        // Starts idle; loadBooks always transitions into loading explicitly, so the
        // emitted sequence is loading -> result on every load including retries.
        initialState = BookListState(),
        onCreate = { loadBooks() },
    )

    fun loadBooks() = intent {
        reduce { state.copy(isLoading = true, errorMessage = null) }

        when (val result = bookApi.getBooks()) {
            is NetworkResult.Success ->
                reduce {
                    state.copy(isLoading = false, books = result.data, errorMessage = null)
                }

            is NetworkResult.ConnectivityFailure -> {
                reduce { state.copy(isLoading = false, errorMessage = CONNECTIVITY_MESSAGE) }
                postSideEffect(BookListSideEffect.ShowError(CONNECTIVITY_MESSAGE))
            }

            is NetworkResult.MalformedResponse -> {
                reduce { state.copy(isLoading = false, errorMessage = MALFORMED_MESSAGE) }
                postSideEffect(BookListSideEffect.ShowError(MALFORMED_MESSAGE))
            }
        }
    }

    fun retry() = loadBooks()

    internal companion object {
        const val CONNECTIVITY_MESSAGE = "Could not reach the server. Check your connection."
        const val MALFORMED_MESSAGE = "The server returned data this app could not read."
    }
}