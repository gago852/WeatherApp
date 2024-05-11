package com.gago.weatherapp.data.repository

import com.gago.weatherapp.data.remote.OpenWeatherMapApi
import com.gago.weatherapp.data.remote.dto.WeatherDto
import com.gago.weatherapp.domain.repository.WeatherRepository
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
    ): WeatherDto {
        return weatherApi.getWeather(latitude, longitude, apiKey, lang, units)
    }
}