package com.gago.weatherapp.data.repository

import com.gago.weatherapp.data.remote.OpenWeatherMapApi
import com.gago.weatherapp.data.remote.dto.airpollution.toAqi
import com.gago.weatherapp.data.remote.dto.forecast.toForecastFiveDays
import com.gago.weatherapp.data.remote.dto.weather.toWeather
import com.gago.weatherapp.data.remote.interceptor.NoNetworkException
import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: OpenWeatherMapApi
) : WeatherRepository {

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Result<CurrentWeather, DataError.Network> {

        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApi.getWeather(latitude, longitude, apiKey, lang, units)
                Result.Success(
                    response.toWeather().copy(aqi = fetchAqi(latitude, longitude, apiKey))
                )
            } catch (e: Exception) {

                when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            401 -> Result.Error(DataError.Network.UNAUTHORIZED)
                            404 -> Result.Error(DataError.Network.NOT_FOUND)
                            429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
                            else -> Result.Error(DataError.Network.UNKNOWN)
                        }
                    }

                    is NoNetworkException -> Result.Error(DataError.Network.NO_INTERNET)
                    is CancellationException -> throw e
                    else -> {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        Result.Error(DataError.Network.UNKNOWN)
                    }
                }
            }
        }
    }

    /** AQI is a nice-to-have: any failure degrades to null instead of failing the weather. */
    private suspend fun fetchAqi(latitude: Double, longitude: Double, apiKey: String): Int? {
        return try {
            weatherApi.getAirPollution(latitude, longitude, apiKey).toAqi()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getForecastFiveDays(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Result<Forecast, DataError.Network> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApi.getForecast(latitude, longitude, apiKey, lang, units, 40)
                Result.Success(response.toForecastFiveDays())
            } catch (e: Exception) {

                when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            401 -> Result.Error(DataError.Network.UNAUTHORIZED)
                            404 -> Result.Error(DataError.Network.NOT_FOUND)
                            429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
                            else -> Result.Error(DataError.Network.UNKNOWN)
                        }
                    }

                    is NoNetworkException -> Result.Error(DataError.Network.NO_INTERNET)
                    is CancellationException -> throw e
                    else -> {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        Result.Error(DataError.Network.UNKNOWN)
                    }
                }
            }
        }
    }
}