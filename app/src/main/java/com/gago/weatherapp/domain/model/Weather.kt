package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Weather(
    val currentWeather: CurrentWeather,
    val forecast: Forecast
)
