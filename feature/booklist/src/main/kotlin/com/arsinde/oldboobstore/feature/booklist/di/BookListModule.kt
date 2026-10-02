package com.arsinde.oldboobstore.feature.booklist.di

import com.arsinde.oldboobstore.feature.booklist.BookListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Injection declarations owned by `:feature:booklist`.
 *
 * Contains nothing belonging to another module. `:app` is the only place that
 * aggregates this into the application-wide graph.
 */
val bookListModule: Module = module {
    viewModel { BookListViewModel(bookApi = get()) }
}