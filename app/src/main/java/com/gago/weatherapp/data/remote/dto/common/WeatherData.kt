package com.gago.weatherapp.data.remote.dto.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.gago.weatherapp.domain.model.WeatherData as DomainWeatherData

@JsonClass(generateAdapter = true)
data class WeatherData(
    @Json(name = "feels_like")
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val temp: Double,
    @Json(name = "temp_max")
    val tempMax: Double,
    @Json(name = "temp_min")
    val tempMin: Double
)

fun WeatherData.toDomain(): DomainWeatherData = DomainWeatherData(
    feelsLike = feelsLike,
    humidity = humidity,
    pressure = pressure,
    temp = temp,
    tempMax = tempMax,
    tempMin = tempMin
)
