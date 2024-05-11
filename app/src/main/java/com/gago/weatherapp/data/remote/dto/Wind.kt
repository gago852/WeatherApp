package com.gago.weatherapp.data.remote.dto

data class Wind(
    val deg: Int,
    val gust: Double? = 0.0,
    val speed: Double? = 0.0
)