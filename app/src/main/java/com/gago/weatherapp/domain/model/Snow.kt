package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Snow(
    val oneHour: Double? = null,
    val threeHour: Double? = null
)
