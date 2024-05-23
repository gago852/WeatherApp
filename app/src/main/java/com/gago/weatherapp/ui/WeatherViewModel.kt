package com.gago.weatherapp.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.BuildConfig
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.domain.location.LocationTracker
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.ui.utils.MeasureUnit
import com.gago.weatherapp.ui.utils.getCurrentLanguage
import com.gago.weatherapp.ui.utils.getErrorText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val dataStore: DataStore<Settings>,
    private val context: Application
) : ViewModel() {

    var state by mutableStateOf(WeatherState())
        private set

    val settings = dataStore.data.catch {
        emit(Settings())
    }

    var unitOfMetrics = MeasureUnit.METRIC

    fun loadLocationWeather() {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            locationTracker.getCurrentLocation()?.let { location ->
                try {
                    val result =
                        repository.getWeather(
                            location.latitude,
                            location.longitude,
                            BuildConfig.API_KEY,
                            getCurrentLanguage(context),
                            unitOfMetrics.unit
                        )
                    when (result) {
                        is Result.Success -> {
                            state = state.copy(
                                weatherCurrent = result.data,
                                error = null,
                                isLoading = false
                            )
                        }

                        is Result.Error -> {

                            var error: Int? = getErrorText(result.error)

                            state = state.copy(
                                weatherCurrent = null,
                                error = error,
                                isLoading = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WeatherViewModel", e.message.toString())
                }


            } ?: kotlin.run {
                state = state.copy(
                    isLoading = false,
                    error = R.string.error_location
                )
            }
        }
    }

    fun loadAnotherWeather(settings: Settings){

    }
}