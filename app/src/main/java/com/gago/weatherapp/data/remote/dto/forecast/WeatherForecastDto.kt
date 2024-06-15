package com.gago.weatherapp.data.remote.dto.forecast

import com.gago.weatherapp.data.remote.dto.common.Clouds
import com.gago.weatherapp.data.remote.dto.common.Rain
import com.gago.weatherapp.data.remote.dto.common.Snow
import com.gago.weatherapp.data.remote.dto.common.WeatherConditionDto
import com.gago.weatherapp.data.remote.dto.common.WeatherData
import com.gago.weatherapp.data.remote.dto.common.Wind
import com.gago.weatherapp.data.remote.dto.common.toWeatherCondition
import com.gago.weatherapp.domain.model.WeatherForecast
import com.gago.weatherapp.domain.utils.convertDateWithoutTimeFromUnixLocatedTimeZoneToDayOfWeek
import com.squareup.moshi.Json

data class WeatherForecastDto(
    val dt: Int,
    @field:Json(name = "dt_txt")
    val dtTxt: String,
    val clouds: Clouds,
    val main: WeatherData,
    val pop: Double,
    @field:Json(name = "sys")
    val partOfTheDay: PartOfTheDay,
    val visibility: Int,
    val wind: Wind,
    val weather: List<WeatherConditionDto>,
    val rain: Rain?,
    val snow: Snow?
)

fun WeatherForecastDto.toWeatherForecast(timeZoneOffset: Long): WeatherForecast {
    return WeatherForecast(
        calculatedTime = convertDateWithoutTimeFromUnixLocatedTimeZoneToDayOfWeek(
            unixTime = dt.toLong(),
            timeZoneOffset = timeZoneOffset
        ),
        calculatedTimeFromServer = dtTxt,
        mainData = main,
        probabilityOfPrecipitation = pop,
        partOfTheDay = partOfTheDay,
        visibility = visibility,
        wind = wind,
        weatherCondition = weather.first().toWeatherCondition(),
        rain = rain,
        snow = snow
    )
}