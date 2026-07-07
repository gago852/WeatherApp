package com.gago.weatherapp.data.remote.dto.forecast

import com.gago.weatherapp.data.remote.dto.common.Coord
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class City(
    val id: Int,
    val name: String,
    val country: String,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int,
    val coord: Coord
)