package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class City(
    val id: Int,
    val name: String,
    val country: String,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int,
    val coord: GeoCoordinate
)
