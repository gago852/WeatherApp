package com.gago.weatherapp.domain.repository

import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result

interface WeatherRepository {

    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Result<Weather, DataError.Network>

    suspend fun getForecastFiveDays(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Result<Forecast, DataError.Network>
}