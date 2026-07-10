package com.gago.weatherapp.data.remote.dto.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.gago.weatherapp.domain.model.Snow as DomainSnow

@JsonClass(generateAdapter = true)
data class Snow(
    @Json(name = "1h")
    val oneHour: Double? = null,
    @Json(name = "3h")
    val threeHour: Double? = null
)

fun Snow.toDomain(): DomainSnow = DomainSnow(
    oneHour = oneHour,
    threeHour = threeHour
)
