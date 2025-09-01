package com.gago.weatherapp.data.remote.dto.forecast

import com.gago.weatherapp.domain.model.Forecast
import com.squareup.moshi.Json
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

data class ForecastDto(
    val cod: String,
    val message: Int,
    val city: City,
    @Json(name = "cnt")
    val count: Int,
    @Json(name = "list")
    val listWeatherForecast: List<WeatherForecastDto>
)

fun ForecastDto.toForecastFiveDays(): Forecast {
    return Forecast(
        city = city,
        forecastCount = count,
        listForecastWeather = listWeatherForecast.map {
            val forecastTimeLocal =
                ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(it.dt.toLong()),
                    ZoneOffset.ofTotalSeconds(city.timezone)
                )
            it to forecastTimeLocal
        }.groupBy {
            it.second.toLocalDate()
        }.mapValues { (_, listWeatherForecast) ->
            listWeatherForecast.minByOrNull {
                abs(
                    ChronoUnit.MINUTES.between(
                        it.second.toLocalTime(),
                        12.toLocalTime()
                    )
                )
            }!!.first
        }.entries.sortedBy { it.key }.take(5).map { it.value }.map {
            it.toWeatherForecast(city.timezone.toLong())
        }
    )
}

fun Int.toLocalTime(): LocalTime = LocalDateTime.of(1970, 1, 1, this, 0).toLocalTime()