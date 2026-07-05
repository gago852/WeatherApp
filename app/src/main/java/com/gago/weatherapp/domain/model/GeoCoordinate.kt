package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
    val name: String?
)
