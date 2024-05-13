package com.gago.weatherapp.ui

import com.gago.weatherapp.domain.model.Weather

data class WeatherState(
    val weatherCurrent: Weather? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
