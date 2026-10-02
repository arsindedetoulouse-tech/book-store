package com.arsinde.oldboobstore.core.network.api

import com.arsinde.oldboobstore.core.model.Book
import com.arsinde.oldboobstore.core.network.NetworkResult
import com.arsinde.oldboobstore.core.network.dto.BookListResponseDto
import com.arsinde.oldboobstore.core.network.dto.toDomain
import com.arsinde.oldboobstore.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * [BookApi] over a Ktor client.
 *
 * The only place in the project that knows both the HTTP library and the wire
 * format. Both stay invisible to callers.
 */
internal class KtorBookApi(
    private val client: HttpClient,
    private val baseUrl: String,
) : BookApi {

    override suspend fun getBooks(): NetworkResult<List<Book>> = safeApiCall {
        client.get("$baseUrl/books").body<BookListResponseDto>().docs.map { it.toDomain() }
    }

    override suspend fun getBook(id: String): NetworkResult<Book> = safeApiCall {
        client.get("$baseUrl/books/$id").body<BookListResponseDto>()
            .docs
            .firstOrNull { it.id == id }
            ?.toDomain()
            ?: error("No book with id $id")
    }
}