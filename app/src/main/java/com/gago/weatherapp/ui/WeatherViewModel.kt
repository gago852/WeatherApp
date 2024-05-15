package com.gago.weatherapp.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.BuildConfig
import com.gago.weatherapp.domain.location.LocationTracker
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.ui.utils.MeasureUnit
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

    var unitOfMetrics = MeasureUnit.METRIC

    fun loadCurrentWeather() {
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
                            "es",
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

                            var error: String? = null

                            when (result.error) {
                                DataError.Network.REQUEST_TIMEOUT -> {
                                    error = "esta lenteja esta mierda"
                                }

                                DataError.Network.TOO_MANY_REQUESTS -> {
                                    error = "vajale mani"
                                }

                                DataError.Network.NO_INTERNET -> {
                                    error = "paga los datos cachon"
                                }

                                DataError.Network.PAYLOAD_TOO_LARGE -> {
                                    error = "mani te pasastes"
                                }

                                DataError.Network.SERVER_ERROR -> {
                                    error = "monda el server se callo"
                                }

                                DataError.Network.SERIALIZATION -> {
                                    error = "marica tu que mandastes aqui"
                                }

                                DataError.Network.UNAUTHORIZED -> {
                                    error = "corre que llegaron los tombos"
                                }

                                DataError.Network.FORBIDDEN -> {
                                    error = "verga nos echaron"
                                }

                                DataError.Network.NOT_FOUND -> {
                                    error = "eche donde queda esa mondaaa"
                                }

                                DataError.Network.UNKNOWN -> {
                                    error = "que verga isistes"
                                }
                            }

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


            }?: kotlin.run {
                state = state.copy(
                    isLoading = false,
                    error = "Couldn't retrieve location. Make sure to grant permission and enable GPS."
                )
            }
        }
    }
}