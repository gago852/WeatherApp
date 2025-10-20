package com.gago.weatherapp.domain.repository

import com.gago.weatherapp.domain.model.GeoCoordinate
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken

interface PlacesRepository {
    suspend fun autocomplete(
        query: String,
        sessionToken: AutocompleteSessionToken,
        language: String
    ): Result<List<AutocompletePrediction>, DataError.Places>

    suspend fun placeCoordinates(
        placeId: String,
        sessionToken: AutocompleteSessionToken,
        language: String
    ): Result<GeoCoordinate, DataError.Places>
}
