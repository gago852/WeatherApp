package com.gago.weatherapp.domain.usecase

import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import javax.inject.Inject

/**
 * Decides how a refresh should be performed: throttling (skip if the last update is recent
 * and weather is already on screen, unless the language changed since that update) and
 * GPS vs stored-coordinates resolution.
 */
class RefreshWeatherUseCase @Inject constructor() {

    sealed interface RefreshDecision {
        /** No settings available: report a refresh error. */
        data object NoSettings : RefreshDecision

        /** Data is fresh enough and already on screen: nothing to do. */
        data object NotNeeded : RefreshDecision

        /** Refresh using the device location. */
        data object FromGps : RefreshDecision

        /** Refresh the active stored city. */
        data class FromCoordinates(val city: WeatherLocal, val settings: Settings) :
            RefreshDecision

        /** No active city and location permission was not granted. */
        data object NoCityWithoutPermission : RefreshDecision
    }

    operator fun invoke(
        settings: Settings?,
        hasWeatherLoaded: Boolean,
        languageChanged: Boolean = false,
        now: Long = System.currentTimeMillis()
    ): RefreshDecision {
        val setting = settings ?: return RefreshDecision.NoSettings

        if ((now - setting.lastUpdate) <= REFRESH_THROTTLE_MS && hasWeatherLoaded &&
            !languageChanged
        ) {
            return RefreshDecision.NotNeeded
        }

        val activeCity = setting.listWeather.firstOrNull { weatherLocal -> weatherLocal.isActive }
        return when {
            activeCity != null && activeCity.isGps -> RefreshDecision.FromGps
            activeCity != null -> RefreshDecision.FromCoordinates(activeCity, setting)
            setting.permissionAccepted -> RefreshDecision.FromGps
            else -> RefreshDecision.NoCityWithoutPermission
        }
    }

    companion object {
        const val REFRESH_THROTTLE_MS = 60_000L
    }
}
