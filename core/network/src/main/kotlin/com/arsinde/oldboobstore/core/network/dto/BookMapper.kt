package com.arsinde.oldboobstore.core.network.dto

import com.arsinde.oldboobstore.core.model.Book

/**
 * Maps wire-format types to domain types.
 *
 * The mapping lives here, not in `:core:model`, so the domain module stays free
 * of serialization concerns and its shape is never dictated by a payload.
 */
internal fun BookDto.toDomain(): Book = Book(
    id = id,
    title = title,
    author = authors.joinToString(separator = ", "),
    coverImageUrl = coverImage,
    description = description,
)