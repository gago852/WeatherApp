package com.gago.weatherapp.data.remote.dto.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.gago.weatherapp.domain.model.Rain as DomainRain

@JsonClass(generateAdapter = true)
data class Rain(
    @Json(name = "1h")
    val oneHour: Double? = null,
    @Json(name = "3h")
    val threeHour: Double? = null
)

fun Rain.toDomain(): DomainRain = DomainRain(
    oneHour = oneHour,
    threeHour = threeHour
)
