package com.gago.weatherapp.data.remote.dto.weather

import com.gago.weatherapp.domain.model.DayData
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SysDto(
    val country: String,
    val id: Int? = null,
    val sunrise: Long,
    val sunset: Long,
    val type: Int? = null
)

fun SysDto.toDayData(): DayData {
    return DayData(
        sunrise = sunrise,
        sunset = sunset
    )
}