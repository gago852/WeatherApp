package com.gago.weatherapp.data.remote.dto.common

import com.gago.weatherapp.domain.model.Wind as DomainWind
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Wind(
    val deg: Int,
    val gust: Double? = 0.0,
    val speed: Double? = 0.0
)

fun Wind.toDomain(): DomainWind = DomainWind(
    deg = deg,
    gust = gust,
    speed = speed
)
