package com.gago.weatherapp.domain.model

data class WeatherCondition(
    val description: String,
    val icon: WeatherTypeIcon,
    val id: Int,
    val mainCondition: String
)
