package com.gago.weatherapp.domain.usecase

import androidx.datastore.core.DataStore
import com.gago.weatherapp.data.datastore.CachedWeather
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherCache
import com.gago.weatherapp.data.datastore.weatherCacheKey
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Orchestrates the two remote calls needed to show a city: current weather + 5-day forecast.
 * On success it persists the result in the per-city offline cache; on failure it falls back
 * to that cache (flagged as [Output.isFromCache]) so the UI can still show data without
 * network. Fails fast with the first error so the caller reports a single message, and keeps
 * the refresh-throttle bookkeeping: stamps lastUpdate on success and resets it on failure so
 * the next refresh is not skipped.
 */
class GetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository,
    private val dataStore: DataStore<Settings>,
    private val weatherCache: DataStore<WeatherCache>
) {

    data class Output(
        val weather: Weather,
        val isFromCache: Boolean,
        val fetchedAt: Long
    )

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Result<Output, DataError> {
        return try {
            val currentWeather =
                when (val result =
                    repository.getWeather(latitude, longitude, apiKey, lang, units)) {
                    is Result.Success -> result.data
                    is Result.Error -> return failure(result.error, latitude, longitude)
                }

            when (val forecast =
                repository.getForecastFiveDays(latitude, longitude, apiKey, lang, units)) {
                is Result.Success -> {
                    val weather = Weather(currentWeather = currentWeather, forecast = forecast.data)
                    val now = System.currentTimeMillis()
                    dataStore.updateData { currentSettings ->
                        currentSettings.copy(
                            lastUpdate = now,
                            lastLangUsed = lang
                        )
                    }
                    writeCache(latitude, longitude, weather, now, lang)
                    Result.Success(Output(weather, isFromCache = false, fetchedAt = now))
                }

                is Result.Error -> failure(forecast.error, latitude, longitude)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            runCatching {
                dataStore.updateData { currentSettings -> currentSettings.copy(lastUpdate = 0L) }
            }
            readCache(latitude, longitude)
                ?.let { Result.Success(it) }
                ?: Result.Error(DataError.Local.UNKNOWN)
        }
    }

    /**
     * Resets the throttle so the next refresh retries the network, then serves the cached
     * city if there is one; the original error is only surfaced when the cache is empty.
     */
    private suspend fun failure(
        error: DataError,
        latitude: Double,
        longitude: Double
    ): Result<Output, DataError> {
        dataStore.updateData { currentSettings -> currentSettings.copy(lastUpdate = 0L) }
        return readCache(latitude, longitude)
            ?.let { Result.Success(it) }
            ?: Result.Error(error)
    }

    private suspend fun writeCache(
        latitude: Double,
        longitude: Double,
        weather: Weather,
        fetchedAt: Long,
        lang: String
    ) {
        runCatching {
            weatherCache.updateData { cache ->
                cache.put(
                    weatherCacheKey(latitude, longitude),
                    CachedWeather(weather = weather, fetchedAt = fetchedAt, lang = lang)
                )
            }
        }.onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
    }

    private suspend fun readCache(latitude: Double, longitude: Double): Output? {
        val cached = runCatching {
            weatherCache.data.firstOrNull()?.entries?.get(weatherCacheKey(latitude, longitude))
        }.getOrNull() ?: return null
        return Output(
            weather = cached.weather,
            isFromCache = true,
            fetchedAt = cached.fetchedAt
        )
    }
}
