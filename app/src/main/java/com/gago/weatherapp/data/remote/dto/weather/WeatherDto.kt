package com.gago.weatherapp.data.remote.dto.weather

import com.gago.weatherapp.data.remote.dto.common.Clouds
import com.gago.weatherapp.data.remote.dto.common.Coord
import com.gago.weatherapp.data.remote.dto.common.Rain
import com.gago.weatherapp.data.remote.dto.common.Snow
import com.gago.weatherapp.data.remote.dto.common.WeatherConditionDto
import com.gago.weatherapp.data.remote.dto.common.WeatherData
import com.gago.weatherapp.data.remote.dto.common.Wind
import com.gago.weatherapp.data.remote.dto.common.toWeatherCondition
import com.gago.weatherapp.domain.model.DayData
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.utils.convertDateFromUnixLocalTimeZone

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
    val sys: SysDto,
    val timezone: Int,
    val weather: List<WeatherConditionDto>,
    val wind: Wind
)

fun WeatherDto.toWeather(): Weather {
    return Weather(
        id = id,
        name = name,
        timezone = timezone,
        dayData = sys.toDayData(timezone.toLong()),
        calculatedTime = convertDateFromUnixLocalTimeZone(
            unixTime = dt.toLong()
        ),
        weatherConditions = weather.first().toWeatherCondition(),
        weatherData = main,
        wind = wind,
        rain = rain,
        snow = snow,
        visibility = visibility,
        clouds = clouds.all

    )


}