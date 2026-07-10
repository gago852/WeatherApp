package com.gago.weatherapp.widget

import androidx.annotation.DrawableRes
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherCache
import com.gago.weatherapp.data.datastore.weatherCacheKey
import kotlin.math.roundToInt

/** Everything the 4×1 widget renders; null city means "open the app to pick one". */
data class WidgetData(
    val cityName: String,
    val temperature: String,
    val description: String,
    @DrawableRes val icon: Int
)

/**
 * Resolves the widget content from the two DataStores: the active city comes from Settings
 * and its weather from the offline cache the background sync keeps fresh. Pure function so
 * it is unit-testable.
 */
fun buildWidgetData(settings: Settings, cache: WeatherCache): WidgetData? {
    val activeCity = settings.listWeather.firstOrNull { it.isActive } ?: return null
    val cached = cache.entries[weatherCacheKey(activeCity.lat, activeCity.lon)] ?: return null
    val current = cached.weather.currentWeather
    return WidgetData(
        cityName = current.name,
        temperature = "${current.weatherData.temp.roundToInt()}°",
        description = current.weatherConditions.description,
        icon = current.weatherConditions.icon.weatherIcon
    )
}
