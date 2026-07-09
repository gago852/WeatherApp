package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DayData(
    val sunset: Long,
    val sunrise: Long
)
