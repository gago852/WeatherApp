package com.gago.weatherapp.ui.utils

import com.gago.weatherapp.domain.model.City
import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.model.DayData
import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.model.GeoCoordinate
import com.gago.weatherapp.domain.model.PartOfTheDay
import com.gago.weatherapp.domain.model.Rain
import com.gago.weatherapp.domain.model.Snow
import com.gago.weatherapp.domain.model.WeatherCondition
import com.gago.weatherapp.domain.model.WeatherData
import com.gago.weatherapp.domain.model.WeatherForecast
import com.gago.weatherapp.domain.model.WeatherTypeIcon
import com.gago.weatherapp.domain.model.Wind

object MockData {
    fun getCurrentWeatherList() = listOf(
        CurrentWeather(
            id = 6077243,
            name = "Montreal",
            timezone = -14400,
            dayData =
                DayData(
                    sunrise = 1_717_666_920L,
                    sunset = 1_717_727_880L,
                ),
            calculatedTime = 1_717_651_798L,
            weatherConditions = WeatherCondition(
                description = "cielo claro",
                icon = WeatherTypeIcon.ClearSkyNight,
                id = 800,
                mainCondition = "Clear"
            ),
            weatherData = WeatherData(
                feelsLike = 23.42,
                humidity = 71,
                pressure = 1003,
                temp = 23.19,
                tempMax = 24.27,
                tempMin = 19.43
            ),
            wind = Wind(deg = 140, gust = 0.1, speed = 3.09),
            visibility = 5,
            clouds = 25,
            rain = Rain(oneHour = 1.0, threeHour = 2.0),
            snow = Snow(oneHour = 1.0, threeHour = 3.0)
        )
    )

    fun getForecastWeatherList() = listOf(
        Forecast(
            city = City(
                id = 6077243,
                name = "Montreal",
                country = "CA",
                population = 5,
                sunrise = 123123,
                sunset = 123123,
                timezone = 123123,
                coord = GeoCoordinate(
                    latitude = 123123.2,
                    longitude = 1231232.2,
                    name = null
                )
            ),
            forecastCount = 5,
            listForecastWeather = listOf(
                WeatherForecast(
                    forecastTime = 1_717_651_798L,
                    timeZoneOffset = -14_400L,
                    calculatedTimeFromServer = "2024-06-06 01:29:58",
                    mainData = WeatherData(
                        feelsLike = 23.42,
                        humidity = 71,
                        pressure = 1003,
                        temp = 23.19,
                        tempMax = 24.27,
                        tempMin = 19.43
                    ),
                    probabilityOfPrecipitation = 1.0,
                    partOfTheDay = PartOfTheDay(""),
                    visibility = 5,
                    wind = Wind(deg = 140, gust = 0.1, speed = 3.09),
                    weatherCondition = WeatherCondition(
                        description = "cielo claro",
                        icon = WeatherTypeIcon.ClearSkyNight,
                        id = 800,
                        mainCondition = "Clear"
                    ),
                    rain = Rain(oneHour = 1.0, threeHour = 2.0),
                    snow = Snow(oneHour = 1.0, threeHour = 3.0)
                )
            )
        )
    )

    fun getWeatherForecast() = WeatherForecast(
        forecastTime = 1_717_651_798L,
        timeZoneOffset = -14_400L,
        calculatedTimeFromServer = "2024-06-06 01:29:58",
        mainData = WeatherData(
            feelsLike = 23.42,
            humidity = 71,
            pressure = 1003,
            temp = 23.19,
            tempMax = 24.27,
            tempMin = 19.43
        ),
        probabilityOfPrecipitation = 1.0,
        partOfTheDay = PartOfTheDay(""),
        visibility = 5,
        wind = Wind(deg = 140, gust = 0.1, speed = 3.09),
        weatherCondition = WeatherCondition(
            description = "cielo claro",
            icon = WeatherTypeIcon.ClearSkyNight,
            id = 800,
            mainCondition = "Clear"
        ),
        rain = Rain(oneHour = 1.0, threeHour = 2.0),
        snow = Snow(oneHour = 1.0, threeHour = 3.0)
    )
}