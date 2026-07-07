package com.gago.weatherapp.domain.usecase

import androidx.datastore.core.DataStore
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.utils.Result
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Background refresh of the active city: fetches through [GetWeatherUseCase] (which updates
 * the offline cache read by the app and the widget) and decides whether the daily summary
 * notification is due. Kept out of the Worker so the logic is unit-testable on the JVM.
 */
class SyncActiveCityUseCase @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val dataStore: DataStore<Settings>
) {

    sealed interface Outcome {
        /** Nothing to sync (no stored active city). */
        data object NoActiveCity : Outcome

        /** Fresh data fetched and cached; [notify] asks the caller to post the summary. */
        data class Refreshed(val weather: Weather, val notify: Boolean) : Outcome

        /** The fetch did not produce fresh data; the caller should retry later. */
        data object Failed : Outcome
    }

    suspend operator fun invoke(
        apiKey: String,
        lang: String,
        now: Long = System.currentTimeMillis()
    ): Outcome {
        val settings = dataStore.data.firstOrNull() ?: return Outcome.NoActiveCity
        val activeCity = settings.listWeather.firstOrNull { it.isActive }
            ?: return Outcome.NoActiveCity

        return when (val result = getWeatherUseCase(
            activeCity.lat, activeCity.lon, apiKey, lang, settings.unitOfMeasurement.unit
        )) {
            is Result.Success -> {
                if (result.data.isFromCache) {
                    // the network failed and the use case served the cache: nothing new
                    Outcome.Failed
                } else {
                    val notify = settings.notificationsEnabled &&
                            now - settings.lastNotificationTime >= NOTIFICATION_PERIOD_MS
                    if (notify) {
                        dataStore.updateData { it.copy(lastNotificationTime = now) }
                    }
                    Outcome.Refreshed(result.data.weather, notify)
                }
            }

            is Result.Error -> Outcome.Failed
        }
    }

    companion object {
        /** Slightly under 24 h so a periodic drift never skips a day. */
        const val NOTIFICATION_PERIOD_MS = 22 * 60 * 60 * 1000L
    }
}
