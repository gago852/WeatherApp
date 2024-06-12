package com.gago.weatherapp.domain.model

import com.gago.weatherapp.data.remote.dto.forecast.City

data class Forecast(
    val city: City,
    val forecastCount: Int,
    val listForecastWeather: List<WeatherForecast>
)