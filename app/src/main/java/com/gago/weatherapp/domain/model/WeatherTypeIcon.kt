package com.gago.weatherapp.domain.model

import androidx.annotation.DrawableRes
import com.gago.weatherapp.R

sealed class WeatherTypeIcon(@DrawableRes val weatherIcon: Int) {

    object ClearSkyDay : WeatherTypeIcon(
        weatherIcon = R.drawable.clear_sky_day
    )

    object ClearSkyNight : WeatherTypeIcon(
        weatherIcon = R.drawable.clear_sky_night
    )

    object FlewCloudsDay : WeatherTypeIcon(
        weatherIcon = R.drawable.few_clouds_day
    )

    object FlewCloudsNight : WeatherTypeIcon(
        weatherIcon = R.drawable.few_clouds_night
    )

    object ScatteredClouds : WeatherTypeIcon(
        weatherIcon = R.drawable.scattered_clouds
    )

    object BrokenClouds : WeatherTypeIcon(
        weatherIcon = R.drawable.broken_clouds
    )

    object Showers : WeatherTypeIcon(
        weatherIcon = R.drawable.shower_rain
    )

    object RainDay : WeatherTypeIcon(
        weatherIcon = R.drawable.rain_day
    )

    object RainNight : WeatherTypeIcon(
        weatherIcon = R.drawable.rain_night
    )

    object Thunderstorm : WeatherTypeIcon(
        weatherIcon = R.drawable.thunderstorm
    )

    object Snow : WeatherTypeIcon(
        weatherIcon = R.drawable.snow
    )

    object Mist : WeatherTypeIcon(
        weatherIcon = R.drawable.mist
    )

    companion object {
        fun fromWeatherType(code: String): WeatherTypeIcon {
            return when (code) {
                "01d" -> ClearSkyDay
                "01n" -> ClearSkyNight
                "02d" -> FlewCloudsDay
                "02n" -> FlewCloudsNight
                "03d", "03n" -> ScatteredClouds
                "04d", "04n" -> BrokenClouds
                "09d", "09n" -> Showers
                "10d" -> RainDay
                "10n" -> RainNight
                "11d", "11n" -> Thunderstorm
                "13d", "13n" -> Snow
                "50d", "50n" -> Mist
                else -> ClearSkyDay
            }

        }
    }
}

