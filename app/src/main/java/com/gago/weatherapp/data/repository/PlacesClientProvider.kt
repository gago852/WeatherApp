package com.gago.weatherapp.data.repository

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.gago.weatherapp.BuildConfig
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the Places SDK initialization so the client can be rebuilt when the in-app language
 * changes: the SDK only takes the locale at initialize() time, and existing clients keep the
 * old one, so re-initializing plus recreating the client is the documented way to apply it
 * hot (see issue #4).
 */
@Singleton
class PlacesClientProvider @Inject constructor(
    private val app: Application
) {

    @Volatile
    private var client: PlacesClient? = null

    fun get(): PlacesClient {
        return client ?: synchronized(this) {
            client ?: createClient(currentLocale()).also { client = it }
        }
    }

    /** Re-initializes the SDK with [locale] and swaps the client used from now on. */
    fun reinitialize(locale: Locale) {
        synchronized(this) {
            client = createClient(locale)
        }
    }

    private fun createClient(locale: Locale): PlacesClient {
        Places.initializeWithNewPlacesApiEnabled(app, BuildConfig.PLACES_API_KEY, locale)
        return Places.createClient(app)
    }

    /** Per-app locale when set, device locale otherwise. */
    private fun currentLocale(): Locale {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return if (!appLocales.isEmpty) {
            appLocales[0] ?: app.resources.configuration.locales[0]
        } else {
            app.resources.configuration.locales[0]
        }
    }
}
