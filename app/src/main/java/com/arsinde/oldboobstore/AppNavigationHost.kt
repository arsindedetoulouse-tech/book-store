package com.arsinde.oldboobstore

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.arsinde.oldboobstore.core.navigation.StartDestination
import com.arsinde.oldboobstore.feature.booklist.bookListGraph

/**
 * The single navigation host for the application.
 *
 * Every feature's contribution is invoked exactly once here. The back stack is
 * Compose state, so pushing or popping a destination recomposes without manual
 * invalidation. Destinations are typed `NavKey` values, never strings.
 */
@Composable
fun AppNavigationHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(StartDestination)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider<NavKey> {
            // One line per feature. Adding a feature edits this file only.
            bookListGraph()
        },
    )
}