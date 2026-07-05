package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class WeatherCondition(
    val description: String,
    val icon: WeatherTypeIcon,
    val id: Int,
    val mainCondition: String
)
