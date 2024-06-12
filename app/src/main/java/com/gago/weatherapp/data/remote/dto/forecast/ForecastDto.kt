package com.gago.weatherapp.data.remote.dto.forecast

import com.gago.weatherapp.domain.model.Forecast
import com.squareup.moshi.Json

data class ForecastDto(
    val cod: String,
    val message: Int,
    val city: City,
    @field:Json(name = "cnt")
    val count: Int,
    @field:Json(name = "list")
    val listWeatherForecast: List<WeatherForecastDto>
)

fun ForecastDto.toForecastFiveDays(): Forecast {
    return Forecast(
        city = city,
        forecastCount = count,
        listForecastWeather = listWeatherForecast.filter {
            it.dtTxt.contains("12:00:00")
        }.map {
            it.toWeatherForecast(city.timezone.toLong())
        }
    )
}