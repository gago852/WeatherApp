package com.gago.weatherapp.ui.main.states

import androidx.annotation.StringRes
import com.gago.weatherapp.domain.model.Weather

data class WeatherState(
    val weather: Weather? = null,
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null,
    /** True when [weather] comes from the offline cache instead of a fresh fetch. */
    val isFromCache: Boolean = false,
    /** Epoch millis of the fetch that produced [weather] (cached or fresh). */
    val lastFetchTime: Long? = null
)
