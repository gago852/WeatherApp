package com.gago.weatherapp.ui.main.states

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken

data class SearchCityUiState(
    val searchText: String = "",
    val debouncedText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<AutocompletePrediction> = emptyList(),
    val isVisible: Boolean = false,
    val selectedPlace: AutocompletePrediction? = null,
    val token: AutocompleteSessionToken? = null
)
