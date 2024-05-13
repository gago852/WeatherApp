package com.gago.weatherapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.domain.location.LocationTracker
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    var state by mutableStateOf(WeatherState())
        private set

    fun loadCurrentWeather() {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            locationTracker.getCurrentLocation()?.let { location ->
                when (val result =
                    repository.getWeather(location.latitude, location.longitude, "", "", "")) {
                    is Result.Error -> TODO()
                    is Result.Success -> TODO()
                }
            }
        }
    }


}