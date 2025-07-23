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
import com.gago.weatherapp.ui.main.components.CityResult

@HiltViewModel
class SearchCityViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SearchCityUiState())
    val uiState: StateFlow<SearchCityUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null

    fun onSearchTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(searchText = text, isLoading = true, error = null)
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(DEBOUNCE_DELAY_MS)
            _uiState.value = _uiState.value.copy(debouncedText = text)
            fetchMockResults(text)
        }
    }

    private fun fetchMockResults(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList(), isLoading = false)
                return@launch
            }
            // Simulación de resultados mockeados
            val mockResults = listOf(
                CityResult("Buenos Aires", "Argentina"),
                CityResult("Madrid", "España"),
                CityResult("Paris", "Francia"),
                CityResult("New York", "USA"),
                CityResult("Tokyo", "Japón"),
                CityResult("London", "UK")
            ).filter { it.name.contains(query, ignoreCase = true) || it.country.contains(query, ignoreCase = true) }
            _uiState.value = _uiState.value.copy(searchResults = mockResults, isLoading = false)
        }
    }

    fun onResultClick(result: CityResult) {
        _uiState.value = _uiState.value.copy(selectedPlace = result, isVisible = false)
    }

    fun onClear() {
        _uiState.value = _uiState.value.copy(searchText = "", debouncedText = "", searchResults = emptyList(), error = null)
    }

    fun onDismiss() {
        _uiState.value = _uiState.value.copy(isVisible = false, searchText = "", debouncedText = "", searchResults = emptyList(), error = null)
    }

    fun showOverlay() {
        _uiState.value = _uiState.value.copy(isVisible = true)
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 200L
    }
}

data class SearchCityUiState(
    val searchText: String = "",
    val debouncedText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<CityResult> = emptyList(),
    val isVisible: Boolean = false,
    val selectedPlace: CityResult? = null
) 