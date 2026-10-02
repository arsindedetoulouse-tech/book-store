package com.arsinde.oldboobstore

import android.app.Application
import com.arsinde.oldboobstore.core.network.di.networkModule
import com.arsinde.oldboobstore.feature.booklist.di.bookListModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * The application's composition root.
 *
 * This is the only place in the project where module injection declarations are
 * aggregated. Adding a feature means adding its module here and nowhere else.
 */
class BookStoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BookStoreApplication)
            modules(
                module {
                    single { BASE_URL }
                },
                networkModule,
                bookListModule,
            )
        }
    }

    private companion object {
        /** Placeholder host; replace with the real API base URL. */
        const val BASE_URL = "https://example.test"
    }
}