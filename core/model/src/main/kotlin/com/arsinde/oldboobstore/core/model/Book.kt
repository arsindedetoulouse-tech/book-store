package com.arsinde.oldboobstore.core.model

/**
 * Domain representation of a book.
 *
 * Deliberately carries no serialization annotation: wire format lives in
 * `:core:network`, and this type is also used as OrbitMVI state, whose shape
 * must not be dictated by a remote payload.
 */
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverImageUrl: String?,
    val description: String?,
)