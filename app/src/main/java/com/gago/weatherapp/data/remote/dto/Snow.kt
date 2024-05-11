package com.gago.weatherapp.data.remote.dto

import com.squareup.moshi.Json

data class Snow(
    @field:Json(name = "1h")
    val oneHour: Double? = null,
    @field:Json(name = "3h")
    val threeHour: Double? = null
)