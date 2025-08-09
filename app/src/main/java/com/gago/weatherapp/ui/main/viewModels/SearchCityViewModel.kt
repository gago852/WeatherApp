package com.gago.weatherapp.ui.main.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gago.weatherapp.ui.main.states.SearchCityUiState
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.gago.weatherapp.domain.repository.PlacesRepository
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.domain.model.GeoCoordinate

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
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList(), isLoading = false)
                return@launch
            }
            val token = _uiState.value.token ?: AutocompleteSessionToken.newInstance()
            if (_uiState.value.token == null) {
                _uiState.value = _uiState.value.copy(token = token)
            }
            when (val result = placesRepository.autocomplete(query, token, language = "")) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(searchResults = result.data, isLoading = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(searchResults = emptyList(), isLoading = false, error = "Error fetching predictions")
                }
            }
        }
    }

    fun onResultClick(result: AutocompletePrediction) {
        viewModelScope.launch {
            val token = _uiState.value.token ?: AutocompleteSessionToken.newInstance()
            if (_uiState.value.token == null) {
                _uiState.value = _uiState.value.copy(token = token)
            }
            when (val fetch = placesRepository.placeCoordinates(result.placeId, token, language = "")) {
                is Result.Success -> {
                    _selectedGeoCoordinate.value = fetch.data
                    _uiState.value = _uiState.value.copy(selectedPlace = result, isVisible = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(error = "Error fetching place details")
                }
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