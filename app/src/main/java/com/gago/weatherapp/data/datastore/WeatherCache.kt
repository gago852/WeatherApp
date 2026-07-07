package com.gago.weatherapp.data.datastore

import com.gago.weatherapp.domain.model.Weather
import kotlinx.serialization.Serializable
import java.util.Locale

/** One cached fetch: the full domain model plus when and in which language it was fetched. */
@Serializable
data class CachedWeather(
    val weather: Weather,
    val fetchedAt: Long,
    val lang: String
)

/**
 * Last known weather per city, keyed by [weatherCacheKey]. Kept in its own DataStore file
 * (separate from [Settings]) so app startup does not pay for deserializing it.
 */
@Serializable
data class WeatherCache(
    val entries: Map<String, CachedWeather> = emptyMap()
) {
    /** Adds or replaces an entry, evicting the oldest ones beyond [MAX_ENTRIES]. */
    fun put(key: String, entry: CachedWeather): WeatherCache {
        val updated = entries + (key to entry)
        val pruned = updated.entries
            .sortedByDescending { it.value.fetchedAt }
            .take(MAX_ENTRIES)
            .associate { it.key to it.value }
        return WeatherCache(pruned)
    }

    companion object {
        const val MAX_ENTRIES = 10
    }
}

/**
 * Coordinates rounded to two decimals (~1 km) so consecutive GPS fixes of the same place
 * resolve to the same entry.
 */
fun weatherCacheKey(latitude: Double, longitude: Double): String =
    String.format(Locale.US, "%.2f,%.2f", latitude, longitude)
