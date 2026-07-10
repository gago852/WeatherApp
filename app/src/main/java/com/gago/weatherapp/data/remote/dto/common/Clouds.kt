package com.gago.weatherapp.data.remote.dto.common

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Clouds(
    val all: Int
)