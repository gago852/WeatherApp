package com.gago.weatherapp.data.datastore

import kotlinx.serialization.Serializable

/** One successful city search, replayable from the overlay without hitting Places again. */
@Serializable
data class SearchHistoryEntry(
    val name: String,
    val lat: Double,
    val lon: Double
)
