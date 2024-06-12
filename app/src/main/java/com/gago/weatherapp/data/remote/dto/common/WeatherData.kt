package com.gago.weatherapp.data.remote.dto.common

import com.squareup.moshi.Json

data class WeatherData(
    @field:Json(name = "feels_like")
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val temp: Double,
    @field:Json(name = "temp_max")
    val tempMax: Double,
    @field:Json(name = "temp_min")
    val tempMin: Double
)