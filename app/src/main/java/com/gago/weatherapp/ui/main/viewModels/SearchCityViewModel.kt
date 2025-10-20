package com.gago.weatherapp.ui.main.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.R
import com.gago.weatherapp.domain.model.GeoCoordinate
import com.gago.weatherapp.domain.repository.PlacesRepository
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.ui.main.states.SearchCityUiState
import com.gago.weatherapp.ui.utils.getPlacesErrorMessage
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchCityViewModel @Inject constructor(
    private val placesRepository: PlacesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchCityUiState())
    val uiState: StateFlow<SearchCityUiState> = _uiState.asStateFlow()

    private val _selectedGeoCoordinate = MutableStateFlow<GeoCoordinate?>(null)
    val selectedGeoCoordinate: StateFlow<GeoCoordinate?> = _selectedGeoCoordinate.asStateFlow()

    private var debounceJob: Job? = null

    fun onSearchTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(searchText = text, isLoading = true, error = null)
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(DEBOUNCE_DELAY_MS)
            _uiState.value = _uiState.value.copy(debouncedText = text)
            fetchAutocomplete(text)
        }
    }

    private fun fetchAutocomplete(query: String) {
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isLoading = false,
                error = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val token = _uiState.value.token
                if (token == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = R.string.error_search_session
                    )
                    return@launch
                }

                val result = placesRepository.autocomplete(query, token, "es")
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            searchResults = result.data,
                            isLoading = false,
                            error = null
                        )
                    }

                    is Result.Error -> {
                        val errorMessage = getPlacesErrorMessage(result.error)
                        _uiState.value = _uiState.value.copy(
                            searchResults = emptyList(),
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    searchResults = emptyList(),
                    isLoading = false,
                    error = R.string.error_connection
                )
            }
        }
    }

    fun onResultClick(result: AutocompletePrediction) {
        _uiState.value = _uiState.value.copy(
            selectedPlace = result,
            isVisible = false
        )

        viewModelScope.launch {
            try {
                val token = _uiState.value.token
                if (token == null) {
                    _uiState.value = _uiState.value.copy(
                        error = R.string.error_search_session
                    )
                    return@launch
                }

                val geoResult = placesRepository.placeCoordinates(
                    result.placeId,
                    token,
                    "es"
                )

                when (geoResult) {
                    is Result.Success -> {
                        _selectedGeoCoordinate.value = geoResult.data
                        _uiState.value = _uiState.value.copy(error = null)
                    }

                    is Result.Error -> {
                        val errorMessage = getPlacesErrorMessage(geoResult.error)
                        _uiState.value = _uiState.value.copy(error = errorMessage)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = R.string.error_connection
                )
            }
        }
    }

    fun onClear() {
        _uiState.value = _uiState.value.copy(
            searchText = "",
            debouncedText = "",
            searchResults = emptyList(),
            error = null
        )
    }

    fun onDismiss() {
        _uiState.value = _uiState.value.copy(
            isVisible = false,
            searchText = "",
            debouncedText = "",
            searchResults = emptyList(),
            error = null,
            token = null
        )
    }

    fun resetSelectedGeoCoordinate() {
        _selectedGeoCoordinate.value = null
    }

    fun showOverlay() {
        _uiState.value = _uiState.value.copy(
            isVisible = true,
            token = AutocompleteSessionToken.newInstance()
        )
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 200L
    }
} 