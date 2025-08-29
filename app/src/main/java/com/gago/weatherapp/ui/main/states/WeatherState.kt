package com.gago.weatherapp.ui.main.states

import androidx.annotation.StringRes
import com.gago.weatherapp.domain.model.Weather

data class WeatherState(
    val weather: Weather? = null,
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null
)