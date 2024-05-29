package com.gago.weatherapp.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.BuildConfig
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.domain.location.LocationTracker
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.ui.utils.MeasureUnit
import com.gago.weatherapp.ui.utils.getCurrentLanguage
import com.gago.weatherapp.ui.utils.getErrorText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker,
    private val dataStore: DataStore<Settings>,
    private val savedStateHandle: SavedStateHandle,
    private val context: Application
) : ViewModel() {

    var state by mutableStateOf(WeatherState())
        private set

    val settings = dataStore.data.catch {
        emit(Settings())
//        Log.e("StateOnMainScreenSettings", it.message.toString())
    }


    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    fun setPermissionAccepted(isAccepted: Boolean) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(permissionAccepted = isAccepted)
            }
        }

    }

    val a = savedStateHandle.getStateFlow("statup", true)

    suspend fun getInitialSetUp(): Settings? = dataStore.data.firstOrNull()
    suspend fun initialStartUp() {
        savedStateHandle["statup"] = false
    }

    fun refreshWeather() {

        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )
            val weather = settings.first().listWeather.first { it.isActive }
            if (weather.isGps) {
                loadWeatherFromGpsAsync()
            } else {
                getWeatherFromApi(weather.lat, weather.lon, settings.first())
            }
        }
    }


    fun loadLocationWeather() {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            loadWeatherFromGpsAsync()
        }
    }

    private suspend fun loadWeatherFromGpsAsync() {
        locationTracker.getCurrentLocation()?.let { location ->
            val setting = settings.first()
            val name = getWeatherFromApi(location.latitude, location.longitude, setting)

            name?.let { namea ->

                var weatherFromGps = setting.listWeather.find { it.isGps }

                var tempList = persistentListOf<WeatherLocal>()
                var tempWeather: WeatherLocal

                weatherFromGps?.let { localWeather ->
                    val indexActual = setting.listWeather.indexOf(localWeather)
                    tempWeather = localWeather.copy(
                        isActive = true,
                        isGps = true,
                        lat = location.latitude,
                        lon = location.longitude
                    )
                    tempList = setting.listWeather.set(indexActual, tempWeather)
                } ?: {
                    tempWeather = WeatherLocal(
                        lat = location.latitude,
                        lon = location.longitude,
                        isActive = true,
                        isGps = true,
                        name = namea
                    )
                    tempList = setting.listWeather.add(tempWeather)
                }

                dataStore.updateData {
                    it.copy(listWeather = tempList)
                }
            }

        } ?: kotlin.run {
            state = state.copy(
                isLoading = false,
                error = R.string.error_location
            )
        }
    }

    fun loadAnotherWeather(settings: Settings) {

        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            dataStore.updateData {
                settings
            }

            try {
                val weather = settings.listWeather.first { it.isActive }

                getWeatherFromApi(weather.lat, weather.lon, settings)
            } catch (e: NoSuchElementException) {

                val error: Int = getErrorText(DataError.Local.UNKNOWN)
                state = state.copy(
                    weatherCurrent = null,
                    error = error,
                    isLoading = false
                )
            }

        }

    }

    fun loadWeatherFromCurrent(weather: WeatherLocal) {

        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            getWeatherFromApi(weather.lat, weather.lon, settings.first())
        }
    }

    private suspend fun getWeatherFromApi(
        latitude: Double,
        longitude: Double,
        settings: Settings
    ): String? {
        try {
            val result =
                repository.getWeather(
                    latitude,
                    longitude,
                    BuildConfig.API_KEY,
                    getCurrentLanguage(context),
                    settings.unitOfMeasurement.unit
                )
            when (result) {
                is Result.Success -> {
                    state = state.copy(
                        weatherCurrent = result.data,
                        error = null,
                        isLoading = false
                    )
                    return result.data.name
                }

                is Result.Error -> {

                    val error: Int = getErrorText(result.error)

                    state = state.copy(
                        weatherCurrent = null,
                        error = error,
                        isLoading = false
                    )
                    return null
                }
            }
        } catch (e: Exception) {
            val error: Int = getErrorText(DataError.Local.UNKNOWN)
            state = state.copy(
                weatherCurrent = null,
                error = error,
                isLoading = false
            )
            Log.e("WeatherViewModel", e.message.toString())
            return null
        }
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

}