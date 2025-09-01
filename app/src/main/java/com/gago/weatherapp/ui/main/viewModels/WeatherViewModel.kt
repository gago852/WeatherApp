package com.gago.weatherapp.ui.main.viewModels

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
import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.ui.main.states.WeatherState
import com.gago.weatherapp.ui.utils.ONE_MINUTE_IN_MILLIS
import com.gago.weatherapp.ui.utils.ReasonsForRefresh
import com.gago.weatherapp.ui.utils.getCurrentLanguage
import com.gago.weatherapp.ui.utils.getErrorText
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    val settings = dataStore.data.catch { throwable ->
        emit(Settings())
//        Log.e("StateOnMainScreenSettings", throwable.message.toString())
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
            dataStore.updateData { currentSettings ->
                currentSettings.copy(permissionAccepted = isAccepted)
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
    fun initialStartUp(isFirstTime: Boolean) {
        savedStateHandle["statup"] = false
        reasonForRefresh = ReasonsForRefresh.PULL
        if (isFirstTime) {
            state = state.copy(
                isLoading = false
            )
        }
        viewModelScope.launch {
            dataStore.updateData { currentSettings -> currentSettings.copy(lastUpdate = 0L) }
        }
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

                if (((currentDate - settingNotNull.lastUpdate) > ONE_MINUTE_IN_MILLIS) || state.weather == null) {
                    Log.d("WeatherViewModel", "need to refresh")
                    val weather =
                        settingNotNull.listWeather.firstOrNull { weatherLocal -> weatherLocal.isActive }
                    weather?.let { activeWeather ->
                        if (activeWeather.isGps) {
                            loadWeatherFromGpsAsync()
                        } else {
                            getWeatherFromApi(
                                activeWeather.lat,
                                activeWeather.lon,
                                settingNotNull
                            )
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
                    delay(500)
                    state = state.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } ?: run {
                delay(500)
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

            val setting = settings.first()

            val listDeactivated = setting.listWeather.map { weatherLocal ->
                weatherLocal.copy(isActive = false)
            }.toPersistentList()

            dataStore.updateData { currentSettings -> currentSettings.copy(listWeather = listDeactivated) }

            loadWeatherFromGpsAsync()
        }
    }

    private suspend fun loadWeatherFromGpsAsync() {
        locationTracker.getCurrentLocation()?.also { location ->
            val setting = settings.first()
            val name = getWeatherFromApi(
                location.latitude,
                location.longitude,
                setting
            )

            name?.let { weatherName ->

                val weatherFromGps = setting.listWeather.find { weatherLocal -> weatherLocal.isGps }

                var tempWeather: WeatherLocal

                weatherFromGps?.let { localWeather ->
                    val indexActual = setting.listWeather.indexOf(localWeather)
                    tempWeather = localWeather.copy(
                        name = weatherName,
                        isActive = true,
                        isGps = true,
                        lat = location.latitude,
                        lon = location.longitude
                    )
                    dataStore.updateData { currentSettings ->
                        currentSettings.copy(listWeather = currentSettings.listWeather.mutate { list ->
                            list[indexActual] = tempWeather
                        })
                    }
                } ?: run {
                    tempWeather = WeatherLocal(
                        lat = location.latitude,
                        lon = location.longitude,
                        isActive = true,
                        isGps = true,
                        name = weatherName
                    )
                    dataStore.updateData { currentSettings ->
                        currentSettings.copy(listWeather = currentSettings.listWeather.mutate { list ->
                            list.add(tempWeather)
                        })
                    }
                }


            }

        } ?: run {
            delay(500)
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

            dataStore.updateData { currentSettings ->
                settings
            }

            try {
                val weather = settings.listWeather.first { weatherLocal -> weatherLocal.isActive }

                getWeatherFromApi(weather.lat, weather.lon, settings)
            } catch (e: NoSuchElementException) {

                val error: Int = getErrorText(DataError.Local.UNKNOWN)
                FirebaseCrashlytics.getInstance().recordException(e)
                delay(500)
                state = state.copy(
                    weather = null,
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
            var currentWeather: CurrentWeather? = null
            val resultCurrentWeather =
                repository.getWeather(
                    latitude,
                    longitude,
                    BuildConfig.API_KEY,
                    getCurrentLanguage(context),
                    settings.unitOfMeasurement.unit
                )
            when (resultCurrentWeather) {
                is Result.Success -> {
                    currentWeather = resultCurrentWeather.data
                }

                is Result.Error -> {

                    val error: Int = getErrorText(resultCurrentWeather.error)
                    delay(500)
                    state = state.copy(
                        error = error,
                        isLoading = false
                    )
                    dataStore.updateData { currentSettings ->
                        currentSettings.copy(lastUpdate = 0L)
                    }
                    return null
                }
            }

            val forecastFiveDays = repository.getForecastFiveDays(
                latitude,
                longitude,
                BuildConfig.API_KEY,
                getCurrentLanguage(context),
                settings.unitOfMeasurement.unit
            )

            when (forecastFiveDays) {
                is Result.Success -> {

                    val weather = Weather(
                        currentWeather = currentWeather,
                        forecast = forecastFiveDays.data
                    )
                    state = state.copy(
                        weather = weather,
                        error = null,
                        isLoading = false
                    )
                    dataStore.updateData { currentSettings ->
                        currentSettings.copy(lastUpdate = System.currentTimeMillis())
                    }
                    return currentWeather.name
                }

                is Result.Error -> {
                    val error: Int = getErrorText(forecastFiveDays.error)
                    delay(500)
                    state = state.copy(
                        error = error,
                        isLoading = false
                    )
                    dataStore.updateData { currentSettings ->
                        currentSettings.copy(lastUpdate = 0L)
                    }
                    return null
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            val error: Int = getErrorText(DataError.Local.UNKNOWN)
            delay(500)
            state = state.copy(
                error = error,
                isLoading = false
            )
            Log.e("WeatherViewModel", e.message.toString())
            dataStore.updateData { currentSettings ->
                currentSettings.copy(lastUpdate = 0L)
            }
            return null
        }
    }

    fun loadWeatherFromSearch(latitude: Double, longitude: Double, cityName: String) {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            try {
                // 1. Crear o actualizar WeatherLocal para la nueva ciudad
                val newWeatherLocal = WeatherLocal(
                    lat = latitude,
                    lon = longitude,
                    isActive = true,
                    isGps = false,
                    name = cityName
                )

                // 2. Actualizar Settings con la nueva ciudad activa
                dataStore.updateData { currentSettings ->
                    // Buscar si ya existe una ciudad con las mismas coordenadas
                    val existingCityIndex = currentSettings.listWeather.indexOfFirst { weather ->
                        weather.lat == latitude && weather.lon == longitude
                    }

                    val updatedList = if (existingCityIndex != -1) {
                        // Reemplazar la ciudad existente y desactivar las demás sin mutación en iteración
                        currentSettings.listWeather.mapIndexed { index, weather ->
                            if (index == existingCityIndex) newWeatherLocal else weather.copy(
                                isActive = false
                            )
                        }
                    } else {
                        // Desactivar todas y agregar la nueva ciudad al final
                        currentSettings.listWeather.map { weather -> weather.copy(isActive = false) } + newWeatherLocal
                    }

                    currentSettings.copy(
                        listWeather = updatedList.toPersistentList(),
                        lastUpdate = 0L // Forzar actualización
                    )
                }

                // 3. Obtener el clima de la nueva ciudad
                val currentSettings = settings.first()
                getWeatherFromApi(latitude, longitude, currentSettings)

            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                val error: Int = getErrorText(DataError.Local.UNKNOWN)
                delay(500)
                state = state.copy(
                    error = error,
                    isLoading = false
                )
                Log.e("WeatherViewModel", "Error loading weather from search: ${e.message}")
            }
        }
    }

    fun dismissDialog() {
        if (visiblePermissionDialogQueue.isNotEmpty()) {
            visiblePermissionDialogQueue.removeAt(0)
        }
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
