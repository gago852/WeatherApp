package com.gago.weatherapp.ui

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.worker.WeatherSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppStartupViewModel @Inject constructor(
    dataStore: DataStore<Settings>,
    private val application: Application,
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    /** Null until the first DataStore emission; the splash stays up meanwhile. */
    val settings: StateFlow<Settings?> = dataStore.data
        .map<Settings, Settings?> { it }
        .catch { emit(Settings()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            try {
                // Esperar la primera emisión de DataStore para evitar parpadeos por diferidos
                val first = settings.filterNotNull().first()
                // Mantener la sincronización en background alineada con el intervalo guardado
                runCatching {
                    WeatherSyncScheduler.schedule(application, first.refreshIntervalMinutes)
                }
            } catch (_: Exception) {
                // Ignorar errores; no bloquear el splash indefinidamente
            } finally {
                // Pequeño delay para una transición más suave
                delay(300)
                _isReady.value = true
            }
        }
    }
}
