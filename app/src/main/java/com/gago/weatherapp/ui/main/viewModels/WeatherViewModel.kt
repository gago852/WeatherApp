package com.gago.weatherapp.ui.main.viewModels

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
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
import com.gago.weatherapp.domain.usecase.GetWeatherUseCase
import com.gago.weatherapp.domain.usecase.ManageCitiesUseCase
import com.gago.weatherapp.domain.usecase.RefreshWeatherUseCase
import com.gago.weatherapp.domain.usecase.RefreshWeatherUseCase.RefreshDecision
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.ui.main.states.WeatherState
import com.gago.weatherapp.ui.utils.ReasonsForRefresh
import com.gago.weatherapp.ui.utils.getCurrentLanguage
import com.gago.weatherapp.ui.utils.getErrorText
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val manageCitiesUseCase: ManageCitiesUseCase,
    private val locationTracker: LocationTracker,
    private val dataStore: DataStore<Settings>,
    private val savedStateHandle: SavedStateHandle,
    private val context: Application
) : ViewModel() {

    var state by mutableStateOf(WeatherState())
        private set

    val settings = dataStore.data.catch { emit(Settings()) }

    var reasonForRefresh = ReasonsForRefresh.STARTUP
        private set
    var settingChanged: Settings? = null
        private set
    var wentToSettings = false
        private set

    val visiblePermissionDialogQueue = mutableStateListOf<String>()
    val isStartup = savedStateHandle.getStateFlow("startup", true)

    suspend fun setPermissionAccepted(isAccepted: Boolean) {
        try {
            dataStore.updateData { it.copy(permissionAccepted = isAccepted) }
        } catch (e: Exception) {
            Log.e("WeatherViewModel", e.message.toString())
        }
    }

    fun setReasonForRefresh(reason: ReasonsForRefresh) {
        reasonForRefresh = reason
    }

    fun setSettingChanged(setting: Settings?) {
        settingChanged = setting
    }

    fun setWentToSettings(wentToSettings: Boolean) {
        this.wentToSettings = wentToSettings
    }

    fun dismissDialog() {
        if (visiblePermissionDialogQueue.isNotEmpty()) visiblePermissionDialogQueue.removeAt(0)
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

    suspend fun getInitialSetUp(): Settings? = dataStore.data.firstOrNull()

    fun initialStartUp(isFirstTime: Boolean) {
        savedStateHandle["startup"] = false
        reasonForRefresh = ReasonsForRefresh.PULL
        if (isFirstTime) state = state.copy(isLoading = false)
        viewModelScope.launch { dataStore.updateData { it.copy(lastUpdate = 0L) } }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val decision = refreshWeatherUseCase(settings.firstOrNull(), state.weather != null)) {
                is RefreshDecision.NoSettings -> settleAfterDelay(R.string.refresh_error)
                is RefreshDecision.NotNeeded -> settleAfterDelay(error = null)
                is RefreshDecision.FromGps -> loadWeatherFromGpsAsync()
                is RefreshDecision.FromCoordinates ->
                    getWeatherFromApi(decision.city.lat, decision.city.lon, decision.settings)
                is RefreshDecision.NoCityWithoutPermission ->
                    state = state.copy(isLoading = false, error = R.string.refresh_error)
            }
        }
    }

    fun loadLocationWeather() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            manageCitiesUseCase.deactivateAllCities()
            loadWeatherFromGpsAsync()
        }
    }

    private suspend fun loadWeatherFromGpsAsync() {
        locationTracker.getCurrentLocation()?.also { location ->
            val setting = settings.first()
            val name = getWeatherFromApi(location.latitude, location.longitude, setting)
            name?.let { manageCitiesUseCase.upsertGpsCity(location.latitude, location.longitude, it) }
        } ?: settleAfterDelay(R.string.error_location)
    }

    fun loadAnotherWeather(settings: Settings) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            manageCitiesUseCase.applySettings(settings)
            try {
                val weather = settings.listWeather.first { weatherLocal -> weatherLocal.isActive }
                getWeatherFromApi(weather.lat, weather.lon, settings)
            } catch (e: NoSuchElementException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                delay(500)
                state = state.copy(
                    weather = null,
                    error = getErrorText(DataError.Local.UNKNOWN),
                    isLoading = false
                )
            }
        }
    }

    fun loadWeatherFromCurrent(weather: WeatherLocal) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            getWeatherFromApi(weather.lat, weather.lon, settings.first())
        }
    }

    fun loadWeatherFromSearch(latitude: Double, longitude: Double, cityName: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                manageCitiesUseCase.addOrActivateCity(latitude, longitude, cityName)
                getWeatherFromApi(latitude, longitude, settings.first())
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("WeatherViewModel", "Error loading weather from search: ${e.message}")
                settleAfterDelay(getErrorText(DataError.Local.UNKNOWN))
            }
        }
    }

    private suspend fun getWeatherFromApi(
        latitude: Double,
        longitude: Double,
        settings: Settings
    ): String? {
        return when (val result = getWeatherUseCase(
            latitude, longitude, BuildConfig.API_KEY,
            getCurrentLanguage(context), settings.unitOfMeasurement.unit
        )) {
            is Result.Success -> {
                state = state.copy(weather = result.data, error = null, isLoading = false)
                result.data.currentWeather.name
            }
            is Result.Error -> {
                settleAfterDelay(getErrorText(result.error))
                null
            }
        }
    }

    /** Small delay so the refresh indicator does not flicker before settling. */
    private suspend fun settleAfterDelay(@StringRes error: Int?) {
        delay(500)
        state = state.copy(isLoading = false, error = error)
    }
}
