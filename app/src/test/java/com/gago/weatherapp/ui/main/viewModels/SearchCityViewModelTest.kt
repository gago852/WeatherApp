package com.gago.weatherapp.ui.main.viewModels

import com.gago.weatherapp.R
import com.gago.weatherapp.fakes.FakePlacesRepository
import com.gago.weatherapp.rules.MainDispatcherRule
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchCityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `onSearchTextChanged without token sets error and stops loading`() = runTest {
        val repo = FakePlacesRepository()
        val vm = SearchCityViewModel(repo)

        vm.onSearchTextChanged("Madrid")
        advanceUntilIdle()
        // fetchAutocomplete will run after debounce, so advance time
        advanceTimeBy(250)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertThat(state.isLoading, `is`(false))
        assertThat(state.error, `is`(R.string.error_search_session))
    }

    @Test
    fun `short query clears results and loading`() = runTest {
        val repo = FakePlacesRepository()
        val vm = SearchCityViewModel(repo)

        vm.showOverlay()
        vm.onSearchTextChanged("M") // length < 2
        advanceUntilIdle()
        // debounce scheduled but fetchAutocomplete returns early by length
        advanceTimeBy(250)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertThat(state.searchResults.isEmpty(), `is`(true))
        assertThat(state.isLoading, `is`(false))
        assertThat(state.error, org.hamcrest.CoreMatchers.nullValue())
    }

    @Test
    fun `debounced query triggers repository call and ends loading`() = runTest {
        val repo = FakePlacesRepository()
        val vm = SearchCityViewModel(repo)

        vm.showOverlay()
        vm.onSearchTextChanged("Madrid")

        // Wait for debounce 200ms + processing
        advanceTimeBy(250)
        advanceUntilIdle()

        // Repository received the debounced query
        assertThat(repo.lastQuery, `is`("Madrid"))

        val state = vm.uiState.value
        assertThat(state.isLoading, `is`(false))
        assertThat(state.error, org.hamcrest.CoreMatchers.nullValue())
    }
}
