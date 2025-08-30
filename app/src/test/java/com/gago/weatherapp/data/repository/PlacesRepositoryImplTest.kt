package com.gago.weatherapp.data.repository

import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.domain.utils.DataError
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.PlacesStatusCodes
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlacesRepositoryImplTest {

    private lateinit var placesClient: PlacesClient
    private lateinit var repo: PlacesRepositoryImpl
    private lateinit var token: AutocompleteSessionToken

    @Before
    fun setUp() {
        placesClient = mock()
        repo = PlacesRepositoryImpl(placesClient)
        token = AutocompleteSessionToken.newInstance()
    }

    private fun readResource(name: String): String {
        val url = this::class.java.classLoader!!.getResource(name)
            ?: error("Resource not found: $name")
        return url.readText()
    }

    @Test
    fun autocomplete_success_returnsPredictions() = runTest {
        val response: FindAutocompletePredictionsResponse = mock()
        val p1: AutocompletePrediction = mock()
        val p2: AutocompletePrediction = mock()
        whenever(p1.placeId).thenReturn("ChIJd8BlQ2BZwokRAFUEcm_qrcA")
        whenever(p2.placeId).thenReturn("ChIJr6G7sUQayUwR7kP4W4Nf9_U")
        whenever(response.autocompletePredictions).thenReturn(listOf(p1, p2))

        whenever(placesClient.findAutocompletePredictions(any<FindAutocompletePredictionsRequest>()))
            .thenReturn(Tasks.forResult(response))

        val result = repo.autocomplete("Mo", token, "es")

        assertThat(result, instanceOf(Result.Success::class.java))
        val list = (result as Result.Success).data
        assertThat(list.size, `is`(2))
        assertThat(list[0].placeId, `is`("ChIJd8BlQ2BZwokRAFUEcm_qrcA"))
    }

    @Test
    fun autocomplete_error_mapsToDataError() = runTest {
        val ex = ApiException(Status(PlacesStatusCodes.NOT_FOUND))
        whenever(placesClient.findAutocompletePredictions(any<FindAutocompletePredictionsRequest>()))
            .thenReturn(Tasks.forException(ex))

        val result = repo.autocomplete("zzzz", token, "es")

        assertThat(result, instanceOf(Result.Error::class.java))
        assertThat((result as Result.Error).error, `is`(DataError.Places.NOT_FOUND))
    }

    @Test
    fun placeCoordinates_success_returnsGeoCoordinate() = runTest {
        val fetchResponse: FetchPlaceResponse = mock()
        val place: Place = mock()
        val latLng = LatLng(45.5017, -73.5673)
        whenever(place.location).thenReturn(latLng)
        whenever(place.formattedAddress).thenReturn("Montreal, QC, Canada")
        whenever(fetchResponse.place).thenReturn(place)

        whenever(placesClient.fetchPlace(any<FetchPlaceRequest>()))
            .thenReturn(Tasks.forResult(fetchResponse))

        val result = repo.placeCoordinates("somePlaceId", token, "es")

        assertThat(result, instanceOf(Result.Success::class.java))
        val geo = (result as Result.Success).data
        assertThat(geo.latitude, `is`(45.5017))
        assertThat(geo.longitude, `is`(-73.5673))
        assertThat(geo.name, `is`("Montreal, QC, Canada"))
    }

    @Test
    fun placeCoordinates_error_mapsToOverQueryLimit() = runTest {
        val ex = ApiException(Status(PlacesStatusCodes.OVER_QUERY_LIMIT))
        whenever(placesClient.fetchPlace(any<FetchPlaceRequest>()))
            .thenReturn(Tasks.forException(ex))

        val result = repo.placeCoordinates("somePlaceId", token, "es")

        assertThat(result, instanceOf(Result.Error::class.java))
        assertThat((result as Result.Error).error, `is`(DataError.Places.OVER_QUERY_LIMIT))
    }
}
