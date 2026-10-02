package com.arsinde.oldboobstore.core.network.api

import com.arsinde.oldboobstore.core.model.Book
import com.arsinde.oldboobstore.core.network.NetworkResult

/**
 * Typed facade over remote book data.
 *
 * This interface is the entire surface a feature module sees: no HTTP client
 * type, no request builder, no response type. It is an interface precisely so
 * that a test double can replace it without touching the code under test.
 */
interface BookApi {

    suspend fun getBooks(): NetworkResult<List<Book>>

    suspend fun getBook(id: String): NetworkResult<Book>
}