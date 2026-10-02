package com.arsinde.oldboobstore.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire-format representation of a book.
 *
 * Internal to `:core:network`: this type is never exposed to a feature module,
 * which is why it is declared `internal` rather than public.
 */
@Serializable
internal data class BookDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("authors") val authors: List<String> = emptyList(),
    @SerialName("cover_image") val coverImage: String? = null,
    @SerialName("description") val description: String? = null,
)

/** Wire-format response of the list endpoint. */
@Serializable
internal data class BookListResponseDto(
    @SerialName("docs") val docs: List<BookDto> = emptyList(),
)