package com.gago.weatherapp.data.datastore

import com.gago.weatherapp.ui.utils.MeasureUnit
import com.gago.weatherapp.ui.utils.ThemeMode
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val unitOfMeasurement: MeasureUnit = MeasureUnit.METRIC,
    val permissionAccepted: Boolean = false,
    val lastUpdate: Long = 0,

    /** API language of the last successful fetch; empty until the first fetch. */
    val lastLangUsed: String = "",

    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    /** Material You palette on Android 12+; ignored on older devices. */
    val dynamicColor: Boolean = false,

    /** In-app language tag ("en", "es", "fr"); empty means follow the system. */
    val language: String = "",

    /** Auto-refresh staleness threshold in minutes; 0 means manual refresh only. */
    val refreshIntervalMinutes: Int = 0,

    @Serializable(with = MyPersistentListSerializer::class)
    val listWeather: PersistentList<WeatherLocal> = persistentListOf(),

    /** Last successful searches, most recent first (max [MAX_SEARCH_HISTORY]). */
    val searchHistory: List<SearchHistoryEntry> = emptyList()
) {
    companion object {
        const val MAX_SEARCH_HISTORY = 5
    }
}