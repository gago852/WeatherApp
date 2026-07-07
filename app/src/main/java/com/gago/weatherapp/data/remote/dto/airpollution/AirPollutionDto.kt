package com.gago.weatherapp.data.remote.dto.airpollution

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Response of the free-tier /air_pollution endpoint; only the 1–5 index is used. */
@JsonClass(generateAdapter = true)
data class AirPollutionDto(
    @Json(name = "list")
    val entries: List<AirPollutionEntryDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AirPollutionEntryDto(
    val main: AqiMainDto,
    val dt: Long
)

@JsonClass(generateAdapter = true)
data class AqiMainDto(
    val aqi: Int
)

/** OWM Air Quality Index: 1 (good) to 5 (very poor); null if the response is empty. */
fun AirPollutionDto.toAqi(): Int? = entries.firstOrNull()?.main?.aqi
