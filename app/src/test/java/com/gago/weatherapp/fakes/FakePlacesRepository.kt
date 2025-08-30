package com.gago.weatherapp.fakes

import com.gago.weatherapp.domain.model.GeoCoordinate
import com.gago.weatherapp.domain.repository.PlacesRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken

class FakePlacesRepository : PlacesRepository {
    var autocompleteResult: Result<List<AutocompletePrediction>, DataError.Places> =
        Result.Success(emptyList())
    var placeCoordinatesResult: Result<GeoCoordinate, DataError.Places> =
        Result.Success(GeoCoordinate(0.0, 0.0, name = null))

    var lastQuery: String? = null

    override suspend fun autocomplete(
        query: String,
        sessionToken: AutocompleteSessionToken,
        language: String
    ): Result<List<AutocompletePrediction>, DataError.Places> {
        lastQuery = query
        return autocompleteResult
    }

    override suspend fun placeCoordinates(
        placeId: String,
        sessionToken: AutocompleteSessionToken,
        language: String
    ): Result<GeoCoordinate, DataError.Places> = placeCoordinatesResult
}
