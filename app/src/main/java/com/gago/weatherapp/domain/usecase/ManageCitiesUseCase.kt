package com.gago.weatherapp.domain.usecase

import androidx.datastore.core.DataStore
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList
import javax.inject.Inject

/**
 * City management over the settings DataStore: add, activate and remove cities.
 */
class ManageCitiesUseCase @Inject constructor(
    private val dataStore: DataStore<Settings>
) {

    /** Deactivates every stored city (used before switching to the GPS city). */
    suspend fun deactivateAllCities() {
        dataStore.updateData { currentSettings ->
            val listDeactivated = currentSettings.listWeather.map { weatherLocal ->
                weatherLocal.copy(isActive = false)
            }.toPersistentList()
            currentSettings.copy(listWeather = listDeactivated)
        }
    }

    /** Updates the GPS city in place, or adds it if there is none yet. */
    suspend fun upsertGpsCity(latitude: Double, longitude: Double, name: String) {
        val gpsCity = WeatherLocal(
            name = name,
            lat = latitude,
            lon = longitude,
            isActive = true,
            isGps = true
        )
        dataStore.updateData { currentSettings ->
            val existingIndex =
                currentSettings.listWeather.indexOfFirst { weatherLocal -> weatherLocal.isGps }
            currentSettings.copy(listWeather = currentSettings.listWeather.mutate { list ->
                if (existingIndex != -1) {
                    list[existingIndex] = gpsCity
                } else {
                    list.add(gpsCity)
                }
            })
        }
    }

    /**
     * Activates a searched city: replaces an existing entry with the same coordinates or
     * appends a new one, deactivating every other city. Forces the next refresh
     * (lastUpdate = 0).
     */
    suspend fun addOrActivateCity(latitude: Double, longitude: Double, name: String) {
        val newCity = WeatherLocal(
            name = name,
            lat = latitude,
            lon = longitude,
            isActive = true,
            isGps = false
        )
        dataStore.updateData { currentSettings ->
            val existingIndex = currentSettings.listWeather.indexOfFirst { weatherLocal ->
                weatherLocal.lat == latitude && weatherLocal.lon == longitude
            }

            val updatedList = if (existingIndex != -1) {
                currentSettings.listWeather.mapIndexed { index, weatherLocal ->
                    if (index == existingIndex) newCity else weatherLocal.copy(isActive = false)
                }
            } else {
                currentSettings.listWeather.map { weatherLocal ->
                    weatherLocal.copy(isActive = false)
                } + newCity
            }

            currentSettings.copy(
                listWeather = updatedList.toPersistentList(),
                lastUpdate = 0L
            )
        }
    }

    /** Persists a full settings snapshot (city activated or removed from the drawer). */
    suspend fun applySettings(settings: Settings) {
        dataStore.updateData { settings }
    }
}
