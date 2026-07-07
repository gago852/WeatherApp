package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Forecast(
    val city: City,
    val forecastCount: Int,
    val listForecastWeather: List<WeatherForecast>
)