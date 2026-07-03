package com.gago.weatherapp.domain.usecase

import androidx.datastore.core.DataStore
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

/**
 * Orchestrates the two remote calls needed to show a city: current weather + 5-day forecast.
 * Fails fast with the first error so the caller reports a single message, and keeps the
 * refresh-throttle bookkeeping: stamps lastUpdate on success and resets it on failure so the
 * next refresh is not skipped.
 */
class GetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository,
    private val dataStore: DataStore<Settings>
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Result<Weather, DataError> {
        return try {
            val currentWeather =
                when (val result =
                    repository.getWeather(latitude, longitude, apiKey, lang, units)) {
                    is Result.Success -> result.data
                    is Result.Error -> return failure(result.error)
                }

            when (val forecast =
                repository.getForecastFiveDays(latitude, longitude, apiKey, lang, units)) {
                is Result.Success -> {
                    val weather = Weather(currentWeather = currentWeather, forecast = forecast.data)
                    dataStore.updateData { currentSettings ->
                        currentSettings.copy(lastUpdate = System.currentTimeMillis())
                    }
                    Result.Success(weather)
                }

                is Result.Error -> failure(forecast.error)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            runCatching {
                dataStore.updateData { currentSettings -> currentSettings.copy(lastUpdate = 0L) }
            }
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    private suspend fun failure(error: DataError): Result.Error<Weather, DataError> {
        dataStore.updateData { currentSettings -> currentSettings.copy(lastUpdate = 0L) }
        return Result.Error(error)
    }
}
