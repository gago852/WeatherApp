package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
    val name: String?
)
