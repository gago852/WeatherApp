package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class WeatherCondition(
    val description: String,
    @Serializable(with = WeatherTypeIconSerializer::class)
    val icon: WeatherTypeIcon,
    val id: Int,
    val mainCondition: String
)
