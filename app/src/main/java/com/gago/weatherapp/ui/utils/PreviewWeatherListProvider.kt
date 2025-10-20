package com.gago.weatherapp.ui.utils

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.gago.weatherapp.data.remote.dto.common.Coord
import com.gago.weatherapp.data.remote.dto.common.Rain
import com.gago.weatherapp.data.remote.dto.common.Snow
import com.gago.weatherapp.data.remote.dto.common.WeatherData
import com.gago.weatherapp.data.remote.dto.common.Wind
import com.gago.weatherapp.data.remote.dto.forecast.City
import com.gago.weatherapp.data.remote.dto.forecast.PartOfTheDay
import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.model.DayData
import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.model.WeatherCondition
import com.gago.weatherapp.domain.model.WeatherForecast
import com.gago.weatherapp.domain.model.WeatherTypeIcon


class PreviewWeatherListProvider : PreviewParameterProvider<Weather> {
    override val values: Sequence<Weather>
        get() = sequenceOf(
            Weather(
                currentWeather = CurrentWeather(
                    id = 6077243,
                    name = "Montreal",
                    timezone = -14400,
                    dayData =
                        DayData(
                            sunrise = "01:02 AM",
                            sunset = "05:58 PM",
                        ),
                    calculatedTime = "2024-06-06 01:29:58",
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
                ),
                forecast = Forecast(
                    city = City(
                        id = 6077243,
                        name = "Montreal",
                        country = "CA",
                        population = 5,
                        sunrise = 123123,
                        sunset = 123123,
                        timezone = 123123,
                        coord = Coord(
                            lat = 123123.2,
                            lon = 1231232.2
                        )
                    ),
                    forecastCount = 5,
                    listForecastWeather = listOf(
                        WeatherForecast(
                            calculatedTime = "Martes",
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
                        ),
                        WeatherForecast(
                            calculatedTime = "Miercoles",
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
        )
}
