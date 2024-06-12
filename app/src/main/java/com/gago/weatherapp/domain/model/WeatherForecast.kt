package com.gago.weatherapp.domain.model

import com.gago.weatherapp.data.remote.dto.common.Rain
import com.gago.weatherapp.data.remote.dto.common.Snow
import com.gago.weatherapp.data.remote.dto.common.WeatherData
import com.gago.weatherapp.data.remote.dto.common.Wind
import com.gago.weatherapp.data.remote.dto.forecast.PartOfTheDay

data class WeatherForecast(
    val calculatedTime: String,
    val calculatedTimeFromServer: String,
    val mainData: WeatherData,
    val probabilityOfPrecipitation: Int,
    val partOfTheDay: PartOfTheDay,
    val visibility: Int,
    val wind: Wind,
    val weatherCondition: WeatherCondition,
    val rain: Rain?,
    val snow: Snow?
)
