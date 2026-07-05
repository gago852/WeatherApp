package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CurrentWeather(
    val id: Int,
    val name: String,
    val timezone: Int,
    val dayData: DayData,
    val calculatedTime: String,
    val weatherConditions: WeatherCondition,
    val weatherData: WeatherData,
    val wind: Wind,
    val visibility: Int?,
    val clouds: Int,
    val rain: Rain?,
    val snow: Snow?
)
