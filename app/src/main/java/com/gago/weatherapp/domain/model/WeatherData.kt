package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class WeatherData(
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val temp: Double,
    val tempMax: Double,
    val tempMin: Double
)
