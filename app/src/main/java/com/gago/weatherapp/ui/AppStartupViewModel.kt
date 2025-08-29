package com.gago.weatherapp.ui

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.data.datastore.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppStartupViewModel @Inject constructor(
    private val dataStore: DataStore<Settings>,
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        viewModelScope.launch {
            try {
                // Esperar la primera emisión de DataStore para evitar parpadeos por diferidos
                dataStore.data.first()
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
