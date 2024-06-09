package com.gago.weatherapp.ui

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.data.datastore.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dataStore: DataStore<Settings>
) : ViewModel() {

    val settings = dataStore.data.catch {
        emit(Settings())
    }

    fun saveChangeSettings(settings: Settings) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(
                    unitOfMeasurement = settings.unitOfMeasurement,
                    listWeather = settings.listWeather,
                    lastUpdate = 0L
                )
            }
        }
    }
}