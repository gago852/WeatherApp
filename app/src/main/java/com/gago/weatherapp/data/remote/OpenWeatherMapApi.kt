package com.gago.weatherapp.data.remote

import com.gago.weatherapp.data.remote.dto.forecast.ForecastDto
import com.gago.weatherapp.data.remote.dto.weather.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapApi {

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("lang") language: String,
        @Query("units") units: String
    ): WeatherDto

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("lang") language: String,
        @Query("units") units: String,
        @Query("cnt") cnt: Int
    ): ForecastDto
}