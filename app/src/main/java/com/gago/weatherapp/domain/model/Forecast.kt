package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Forecast(
    val city: City,
    val forecastCount: Int,
    val listForecastWeather: List<WeatherForecast>,
    /** Raw 3-hour slots for the next 24 h (up to 8), in chronological order. */
    val hourlyForecast: List<WeatherForecast> = emptyList()
)
