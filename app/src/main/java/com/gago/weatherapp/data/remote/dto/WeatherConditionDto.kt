package com.gago.weatherapp.data.remote.dto

import com.gago.weatherapp.domain.model.WeatherCondition
import com.gago.weatherapp.domain.model.WeatherTypeIcon
import com.squareup.moshi.Json

data class WeatherConditionDto(
    val id: Int,
    @field:Json(name = "main")
    val mainCondition: String,
    val description: String,
    val icon: String
)

fun WeatherConditionDto.toWeatherCondition(): WeatherCondition = WeatherCondition(
    description = description,
    icon = WeatherTypeIcon.fromWeatherType(icon),
    id = id,
    mainCondition = mainCondition
)