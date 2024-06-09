package com.gago.weatherapp.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
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
import com.gago.weatherapp.ui.utils.ONE_MINUTE_IN_MILLIS
import com.gago.weatherapp.ui.utils.ReasonsForRefresh
import com.gago.weatherapp.ui.utils.getCurrentLanguage
import com.gago.weatherapp.ui.utils.getErrorText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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

    var reasonForRefresh = ReasonsForRefresh.STARTUP
        private set

    var settingChanged: Settings? = null
        private set

    var wentToSettings = false
        private set

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    suspend fun setPermissionAccepted(isAccepted: Boolean) {

        try {
            dataStore.updateData {
                it.copy(permissionAccepted = isAccepted)
            }
        } catch (e: Exception) {
            Log.e("WeatherViewModel", e.message.toString())
        }


    }

    val isStartup = savedStateHandle.getStateFlow("statup", true)

    fun setReasonForRefresh(reason: ReasonsForRefresh) {
        reasonForRefresh = reason
    }

    fun setSettingChanged(setting: Settings?) {
        settingChanged = setting
    }

    fun setWentToSettings(wentToSettings: Boolean) {
        this.wentToSettings = wentToSettings
    }

    suspend fun getInitialSetUp(): Settings? = dataStore.data.firstOrNull()
    fun initialStartUp() {
        savedStateHandle["statup"] = false
        reasonForRefresh = ReasonsForRefresh.PULL
    }

    fun refreshWeather() {

        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )
            val setting = settings.firstOrNull()

            val currentDate = System.currentTimeMillis()

            setting?.let { settingNotNull ->

                if ((currentDate - settingNotNull.lastUpdate) > ONE_MINUTE_IN_MILLIS) {
                    Log.d("WeatherViewModel", "need to refresh")
                    val weather = settingNotNull.listWeather.firstOrNull { it.isActive }
                    weather?.let {
                        if (it.isGps) {
                            loadWeatherFromGpsAsync()
                        } else {
                            getWeatherFromApi(it.lat, it.lon, settings.first())
                        }
                    } ?: run {
                        if (settingNotNull.permissionAccepted) {
                            loadWeatherFromGpsAsync()
                        } else {
                            state = state.copy(
                                isLoading = false,
                                error = R.string.refresh_error
                            )
                        }
                    }
                } else {
                    Log.d("WeatherViewModel", "No need to refresh")
                    state = state.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } ?: run {
                state = state.copy(
                    isLoading = false,
                    error = R.string.refresh_error
                )
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
        locationTracker.getCurrentLocation()?.also { location ->
            val setting = settings.first()
            val name = getWeatherFromApi(location.latitude, location.longitude, setting)

            name?.let {

                val weatherFromGps = setting.listWeather.find { weatherLocal -> weatherLocal.isGps }

                var tempWeather: WeatherLocal

                weatherFromGps?.let { localWeather ->
                    val indexActual = setting.listWeather.indexOf(localWeather)
                    tempWeather = localWeather.copy(
                        name = it,
                        isActive = true,
                        isGps = true,
                        lat = location.latitude,
                        lon = location.longitude
                    )
                    dataStore.updateData {
                        it.copy(listWeather = it.listWeather.mutate { list ->
                            list[indexActual] = tempWeather
                        })
                    }
                } ?: run {
                    tempWeather = WeatherLocal(
                        lat = location.latitude,
                        lon = location.longitude,
                        isActive = true,
                        isGps = true,
                        name = it
                    )
                    dataStore.updateData { setting ->
                        setting.copy(listWeather = setting.listWeather.mutate {
                            it.add(tempWeather)
                        })
                    }
                }


            }

        } ?: run {
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
                    dataStore.updateData {
                        it.copy(lastUpdate = System.currentTimeMillis())
                    }
                    return result.data.name
                }

                is Result.Error -> {

                    val error: Int = getErrorText(result.error)

                    state = state.copy(
                        weatherCurrent = null,
                        error = error,
                        isLoading = false
                    )
                    dataStore.updateData {
                        it.copy(lastUpdate = 0L)
                    }
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
            e.printStackTrace()
            Log.e("WeatherViewModel", e.message.toString())
            dataStore.updateData {
                it.copy(lastUpdate = 0L)
            }
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