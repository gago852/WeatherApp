package com.gago.weatherapp.data.repository

import com.gago.weatherapp.domain.model.GeoCoordinate
import com.gago.weatherapp.domain.repository.PlacesRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PlacesRepositoryImpl @Inject constructor(
    private val placesClient: PlacesClient
) : PlacesRepository {

    override suspend fun autocomplete(
        query: String,
        sessionToken: AutocompleteSessionToken,
        language: String
    ): Result<List<AutocompletePrediction>, DataError.Network> {
        return try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setQuery(query)
                .build()
            val response = placesClient.findAutocompletePredictions(request).await()
            Result.Success(response.autocompletePredictions)
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun placeCoordinates(
        placeId: String,
        sessionToken: AutocompleteSessionToken,
        language: String
    ): Result<GeoCoordinate, DataError.Network> {
        return try {
            val request = FetchPlaceRequest.builder(
                placeId,
                listOf(Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS)
            )
                .setSessionToken(sessionToken)
                .build()
            val response = placesClient.fetchPlace(request).await()
            val location = response.place.location
            val formattedAddress = response.place.formattedAddress
            return if (location != null) {
                Result.Success(
                    GeoCoordinate(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        name = formattedAddress
                    )
                )
            } else {
                Result.Error(DataError.Network.UNKNOWN)
            }
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }
}
