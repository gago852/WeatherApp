package com.gago.weatherapp.data.remote.dto.forecast

import com.gago.weatherapp.domain.model.PartOfTheDay as DomainPartOfTheDay
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartOfTheDay(
    val pod: String
)

fun PartOfTheDay.toDomain(): DomainPartOfTheDay = DomainPartOfTheDay(
    pod = pod
)
