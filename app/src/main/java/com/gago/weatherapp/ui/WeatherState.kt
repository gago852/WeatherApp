package com.gago.weatherapp.ui

import androidx.annotation.StringRes
import com.gago.weatherapp.domain.model.Weather

data class WeatherState(
    val weatherCurrent: Weather? = null,
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null
)
