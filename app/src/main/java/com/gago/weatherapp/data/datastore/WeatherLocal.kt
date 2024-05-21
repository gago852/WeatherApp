package com.gago.weatherapp.data.datastore

import kotlinx.serialization.Serializable

@Serializable
data class WeatherLocal(
    val name: String,
    val lat: Double,
    val lon: Double,
    val isActive: Boolean = false,
    val isGps: Boolean = false
)
