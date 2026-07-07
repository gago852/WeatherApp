package com.gago.weatherapp.domain.repository

import com.gago.weatherapp.domain.model.GeoCoordinate
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken

/**
 * The response language follows the locale passed to Places.initialize*(); the Places SDK
 * does not support a per-request language.
 */
interface PlacesRepository {
    suspend fun autocomplete(
        query: String,
        sessionToken: AutocompleteSessionToken
    ): Result<List<AutocompletePrediction>, DataError.Places>

    suspend fun placeCoordinates(
        placeId: String,
        sessionToken: AutocompleteSessionToken
    ): Result<GeoCoordinate, DataError.Places>
}
