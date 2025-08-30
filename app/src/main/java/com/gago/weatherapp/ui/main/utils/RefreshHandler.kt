package com.gago.weatherapp.ui.main.utils

import android.Manifest
import androidx.activity.result.ActivityResultLauncher
import com.gago.weatherapp.ui.main.viewModels.WeatherViewModel
import com.gago.weatherapp.ui.utils.ReasonsForRefresh

suspend fun handleRefresh(
    weatherViewModel: WeatherViewModel,
    locationPermissionResultLauncher: ActivityResultLauncher<String>
) {
    when (weatherViewModel.reasonForRefresh) {
        ReasonsForRefresh.WEATHER_CHANGED -> {
            val settingChanged = weatherViewModel.settingChanged
            settingChanged?.let {
                weatherViewModel.loadAnotherWeather(it)
                weatherViewModel.setSettingChanged(null)
            }
            weatherViewModel.setReasonForRefresh(ReasonsForRefresh.PULL)
        }

        ReasonsForRefresh.STARTUP -> {
            var isFirstTime = true
            val setting = weatherViewModel.getInitialSetUp()
            setting?.let {
                val listWeatherStoredActive = it.listWeather.filter { lit -> lit.isActive }
                val weatherCurrent = listWeatherStoredActive.firstOrNull()
                weatherCurrent?.let { weatherLocal ->
                    isFirstTime = false
                    if (weatherLocal.isGps) {
                        locationPermissionResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    } else {
                        weatherViewModel.loadWeatherFromCurrent(weatherLocal)
                    }
                }
            }
            weatherViewModel.initialStartUp(isFirstTime)
        }

        else -> weatherViewModel.refreshWeather()
    }
}
