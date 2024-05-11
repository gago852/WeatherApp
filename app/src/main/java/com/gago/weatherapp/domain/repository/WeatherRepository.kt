package com.gago.weatherapp.domain.repository

import com.gago.weatherapp.data.remote.dto.WeatherDto

interface WeatherRepository {

    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): WeatherDto

}