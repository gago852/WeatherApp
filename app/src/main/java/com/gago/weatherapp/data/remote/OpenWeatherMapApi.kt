package com.gago.weatherapp.data.remote

import com.gago.weatherapp.data.remote.dto.WeatherDto
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


}