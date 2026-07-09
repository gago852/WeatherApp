package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Forecast(
    val city: City,
    val forecastCount: Int,
    val listForecastWeather: List<WeatherForecast>
)