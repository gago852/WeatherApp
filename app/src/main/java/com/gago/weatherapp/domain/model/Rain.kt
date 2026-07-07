package com.gago.weatherapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Rain(
    val oneHour: Double? = null,
    val threeHour: Double? = null
)
