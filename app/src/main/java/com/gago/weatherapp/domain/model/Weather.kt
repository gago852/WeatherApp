package com.gago.weatherapp.domain.model

import com.gago.weatherapp.data.remote.dto.Clouds
import com.gago.weatherapp.data.remote.dto.Rain
import com.gago.weatherapp.data.remote.dto.Snow
import com.gago.weatherapp.data.remote.dto.Sys
import com.gago.weatherapp.data.remote.dto.WeatherConditionDto
import com.gago.weatherapp.data.remote.dto.WeatherData
import com.gago.weatherapp.data.remote.dto.Wind

data class Weather(
    val id: Int,
    val name: String,
    val timezone: Int,
    val sys: Sys,
    val calculatedTime: String,
    val weatherConditions: WeatherCondition,
    val weatherData: WeatherData,
    val wind: Wind,
    val visibility: Int,
    val clouds: Int,
    val rain: Rain?,
    val snow: Snow?
)
