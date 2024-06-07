package com.gago.weatherapp.data.remote.dto

import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.utils.convertDateFromUnix

data class WeatherDto(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    val main: WeatherData,
    val visibility: Int,
    val name: String,
    val rain: Rain? = null,
    val snow: Snow? = null,
    val sys: Sys,
    val timezone: Int,
    val weather: List<WeatherConditionDto>,
    val wind: Wind
)

fun WeatherDto.toWeather(): Weather {
    return Weather(
        id = id,
        name = name,
        timezone = timezone,
        sys = sys,
        calculatedTime = convertDateFromUnix(dt.toLong()),
        weatherConditions = weather.first().toWeatherCondition(),
        weatherData = main,
        wind = wind,
        rain = rain,
        snow = snow,
        visibility = visibility,
        clouds = clouds.all

    )


}