package com.gago.weatherapp.data.repository

import com.gago.weatherapp.data.remote.OpenWeatherMapApi
import com.gago.weatherapp.data.remote.dto.WeatherDto
import com.gago.weatherapp.data.remote.dto.toWeather
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
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
    ): Result<Weather, DataError.Network> {

        return try {
            val response = weatherApi.getWeather(latitude, longitude, apiKey, lang, units)
            Result.Success(response.toWeather())
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> Result.Error(DataError.Network.UNAUTHORIZED)
                404 -> Result.Error(DataError.Network.NOT_FOUND)
                413 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
                else -> Result.Error(DataError.Network.UNKNOWN)
            }
        }
    }
}