package com.gago.weatherapp.ui.main.viewModels

import com.gago.weatherapp.domain.model.GeoCoordinate
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.fakes.FakePlacesRepository
import com.gago.weatherapp.rules.MainDispatcherRule
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SearchCityViewModelJsonTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Serializable
    private data class AutoItem(
        @SerialName("placeid") val placeId: String,
        @SerialName("primaryName") val primaryName: String,
        @SerialName("secundaryName") val secondaryName: String
    )

    private fun readResource(name: String): String {
        val url = this::class.java.classLoader!!.getResource(name)
            ?: error("Resource not found: $name")
        return url.readText()
    }

    @Test
    fun `autocomplete from JSON populates uiState and repo receives query`() = runTest {
        val repo = FakePlacesRepository()
        val vm = SearchCityViewModel(repo)

        // Prepare predictions from JSON
        val content = readResource("mock_list_autocomplete.json")
        val items = Json.decodeFromString<List<AutoItem>>(content)
        val predictions: List<AutocompletePrediction> = items.map { item ->
            mock<AutocompletePrediction>().also { p ->
                whenever(p.placeId).thenReturn(item.placeId)
            }
        }
        repo.autocompleteResult = Result.Success(predictions)

        vm.showOverlay()
        vm.onSearchTextChanged("Ma")
        advanceTimeBy(250)
        advanceUntilIdle()

        assertThat(repo.lastQuery, `is`("Ma"))
        val state = vm.uiState.value
        assertThat(state.isLoading, `is`(false))
        assertThat(state.error, nullValue())
        assertThat(state.searchResults.size, `is`(predictions.size))
    }

    @Test
    fun `onResultClick uses JSON place details and emits selectedGeoCoordinate`() = runTest {
        val repo = FakePlacesRepository()
        val vm = SearchCityViewModel(repo)

        // Build a single mocked prediction for Montreal
        val prediction = mock<AutocompletePrediction>()
        whenever(prediction.placeId).thenReturn("6077243")

        // Prepare place details from JSON
        val details = readResource("mock_place_details_montreal.json")
        val json = Json.parseToJsonElement(details).jsonObject
        val location = json["location"]!!.jsonObject
        val lat = location["lat"]!!.jsonPrimitive.content.toDouble()
        val lon = location["lon"]!!.jsonPrimitive.content.toDouble()
        val name = json["secundaryName"]!!.toString().trim('"')

        repo.placeCoordinatesResult = Result.Success(GeoCoordinate(lat, lon, name))

        vm.showOverlay()
        vm.onResultClick(prediction)
        advanceUntilIdle()

        val coord = vm.selectedGeoCoordinate.first()
        requireNotNull(coord)
        assertThat(coord.latitude, `is`(lat))
        assertThat(coord.longitude, `is`(lon))
        assertThat(vm.uiState.value.error, nullValue())
    }
}
