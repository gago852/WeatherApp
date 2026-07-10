package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Wind(
    val deg: Int,
    val gust: Double? = 0.0,
    val speed: Double? = 0.0
)
