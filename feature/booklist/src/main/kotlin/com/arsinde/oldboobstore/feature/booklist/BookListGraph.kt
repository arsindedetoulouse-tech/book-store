package com.arsinde.oldboobstore.feature.booklist

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arsinde.oldboobstore.core.navigation.StartDestination
import com.arsinde.oldboobstore.feature.booklist.ui.BookListRoute

/**
 * This feature's contribution to the application's navigation graph.
 *
 * Declared as an extension on [EntryProviderScope] so that `:app` composes it
 * once and no other module needs to know this feature exists. The destination is
 * a typed `NavKey`; there is no string route.
 */
fun EntryProviderScope<NavKey>.bookListGraph() {
    // The entry's content key is derived from the typed destination by Navigation3's
// default, so no string identifier is introduced anywhere.
    entry<StartDestination> {
        BookListRoute(onBookSelected = { })
    }
}