package com.gago.weatherapp.data.remote.dto.weather

import com.gago.weatherapp.domain.model.DayData
import com.gago.weatherapp.domain.utils.getHourFromUnixLocatedTimeZone

data class SysDto(
    val country: String,
    val id: Int,
    val sunrise: Long,
    val sunset: Long,
    val type: Int
)

fun SysDto.toDayData(timeZoneOffset: Long): DayData {
    return DayData(
        sunrise = getHourFromUnixLocatedTimeZone(
            unixTime = sunrise,
            timeZoneOffset = timeZoneOffset
        ),
        sunset = getHourFromUnixLocatedTimeZone(unixTime = sunset, timeZoneOffset = timeZoneOffset)
    )
}