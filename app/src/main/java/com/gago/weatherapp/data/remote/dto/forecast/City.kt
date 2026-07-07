package com.gago.weatherapp.data.remote.dto.forecast

import com.gago.weatherapp.data.remote.dto.common.Coord
import com.squareup.moshi.JsonClass
import com.gago.weatherapp.domain.model.City as DomainCity
import com.gago.weatherapp.domain.model.GeoCoordinate

@JsonClass(generateAdapter = true)
data class City(
    val id: Int,
    val name: String,
    val country: String,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int,
    val coord: Coord
)

fun City.toDomain(): DomainCity = DomainCity(
    id = id,
    name = name,
    country = country,
    population = population,
    sunrise = sunrise,
    sunset = sunset,
    timezone = timezone,
    coord = GeoCoordinate(
        latitude = coord.lat,
        longitude = coord.lon,
        name = name
    )
)
