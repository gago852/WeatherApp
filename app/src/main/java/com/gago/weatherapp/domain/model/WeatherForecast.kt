package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class WeatherForecast(
    val forecastTime: Long,
    val timeZoneOffset: Long,
    val calculatedTimeFromServer: String,
    val mainData: WeatherData,
    val probabilityOfPrecipitation: Double,
    val partOfTheDay: PartOfTheDay,
    val visibility: Int?,
    val wind: Wind,
    val weatherCondition: WeatherCondition,
    val rain: Rain?,
    val snow: Snow?
)
