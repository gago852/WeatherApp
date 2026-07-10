package com.gago.weatherapp.domain.model

import androidx.annotation.DrawableRes
import com.gago.weatherapp.R
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * [key] is the stable identifier persisted by [WeatherTypeIconSerializer]; the drawable id
 * must never be persisted because resource ids change between builds.
 */
sealed class WeatherTypeIcon(@DrawableRes val weatherIcon: Int, val key: String) {

    object ClearSkyDay : WeatherTypeIcon(
        weatherIcon = R.drawable.clear_sky_day,
        key = "clear_sky_day"
    )

    object ClearSkyNight : WeatherTypeIcon(
        weatherIcon = R.drawable.clear_sky_night,
        key = "clear_sky_night"
    )

    object FlewCloudsDay : WeatherTypeIcon(
        weatherIcon = R.drawable.few_clouds_day,
        key = "few_clouds_day"
    )

    object FlewCloudsNight : WeatherTypeIcon(
        weatherIcon = R.drawable.few_clouds_night,
        key = "few_clouds_night"
    )

    object ScatteredClouds : WeatherTypeIcon(
        weatherIcon = R.drawable.scattered_clouds,
        key = "scattered_clouds"
    )

    object BrokenClouds : WeatherTypeIcon(
        weatherIcon = R.drawable.broken_clouds,
        key = "broken_clouds"
    )

    object Showers : WeatherTypeIcon(
        weatherIcon = R.drawable.shower_rain,
        key = "shower_rain"
    )

    object RainDay : WeatherTypeIcon(
        weatherIcon = R.drawable.rain_day,
        key = "rain_day"
    )

    object RainNight : WeatherTypeIcon(
        weatherIcon = R.drawable.rain_night,
        key = "rain_night"
    )

    object Thunderstorm : WeatherTypeIcon(
        weatherIcon = R.drawable.thunderstorm,
        key = "thunderstorm"
    )

    object Snow : WeatherTypeIcon(
        weatherIcon = R.drawable.snow,
        key = "snow"
    )

    object Mist : WeatherTypeIcon(
        weatherIcon = R.drawable.mist,
        key = "mist"
    )

    companion object {
        fun fromKey(key: String): WeatherTypeIcon = when (key) {
            ClearSkyDay.key -> ClearSkyDay
            ClearSkyNight.key -> ClearSkyNight
            FlewCloudsDay.key -> FlewCloudsDay
            FlewCloudsNight.key -> FlewCloudsNight
            ScatteredClouds.key -> ScatteredClouds
            BrokenClouds.key -> BrokenClouds
            Showers.key -> Showers
            RainDay.key -> RainDay
            RainNight.key -> RainNight
            Thunderstorm.key -> Thunderstorm
            Snow.key -> Snow
            Mist.key -> Mist
            else -> ClearSkyDay
        }

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

object WeatherTypeIconSerializer : KSerializer<WeatherTypeIcon> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("WeatherTypeIcon", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: WeatherTypeIcon) =
        encoder.encodeString(value.key)

    override fun deserialize(decoder: Decoder): WeatherTypeIcon =
        WeatherTypeIcon.fromKey(decoder.decodeString())
}
