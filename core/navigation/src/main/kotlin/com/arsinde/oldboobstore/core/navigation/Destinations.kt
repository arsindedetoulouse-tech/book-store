package com.arsinde.oldboobstore.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Entry destination of the application.
 *
 * Route identity is the type itself. There is no string route, URI pattern or
 * serialized route identifier anywhere in this project.
 */
data object StartDestination : NavKey

/**
 * Detail screen for a single book.
 *
 * The argument is a typed property of the destination value, so reading it
 * requires no string parsing.
 */
data class BookDetailDestination(val bookId: String) : NavKey