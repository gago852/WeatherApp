package com.gago.weatherapp.data.remote.dto.forecast

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartOfTheDay(
    val pod: String
)