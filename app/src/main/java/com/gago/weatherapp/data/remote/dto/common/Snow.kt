package com.gago.weatherapp.data.remote.dto.common

import com.squareup.moshi.Json

data class Snow(
    @Json(name = "1h")
    val oneHour: Double? = null,
    @Json(name = "3h")
    val threeHour: Double? = null
)