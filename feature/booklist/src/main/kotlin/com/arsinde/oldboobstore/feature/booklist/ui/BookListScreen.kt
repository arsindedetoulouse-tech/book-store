package com.arsinde.oldboobstore.feature.booklist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arsinde.oldboobstore.core.model.Book
import com.arsinde.oldboobstore.feature.booklist.BookListSideEffect
import com.arsinde.oldboobstore.feature.booklist.BookListState
import com.arsinde.oldboobstore.feature.booklist.BookListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Book list screen.
 *
 * State is collected lifecycle-aware and the view model comes from the injection
 * graph, so the screen constructs no dependency of its own.
 */
@Composable
fun BookListRoute(
    modifier: Modifier = Modifier,
    onBookSelected: (Book) -> Unit = {},
    onError: (String) -> Unit = {},
    viewModel: BookListViewModel = koinViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is BookListSideEffect.ShowError -> onError(effect.message)
            }
        }
    }

    BookListScreen(
        state = state,
        onRetry = viewModel::retry,
        onBookSelected = onBookSelected,
        modifier = modifier,
    )
}

@Composable
fun BookListScreen(
    state: BookListState,
    onRetry: () -> Unit,
    onBookSelected: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Books",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
        )

        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
            )

            state.errorMessage != null -> Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = state.errorMessage, style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = onRetry) { Text("Retry") }
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = state.books, key = { it.id }) { book ->
                    BookRow(book = book, onClick = { onBookSelected(book) })
                }
            }
        }
    }
}

@Composable
private fun BookRow(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = book.title, style = MaterialTheme.typography.titleMedium)
            Text(text = book.author, style = MaterialTheme.typography.bodyMedium)
        }
    }
}