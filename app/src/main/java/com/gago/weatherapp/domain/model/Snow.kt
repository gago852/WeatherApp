package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Snow(
    val oneHour: Double? = null,
    val threeHour: Double? = null
)
