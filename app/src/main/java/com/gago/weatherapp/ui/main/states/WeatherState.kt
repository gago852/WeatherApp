package com.gago.weatherapp.ui.main.states

import androidx.annotation.StringRes
import com.gago.weatherapp.domain.model.Weather

data class WeatherState(
    val weather: Weather? = null,
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null
)