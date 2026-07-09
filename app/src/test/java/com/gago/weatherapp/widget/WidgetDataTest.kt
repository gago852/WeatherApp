package com.gago.weatherapp.widget

import com.gago.weatherapp.data.datastore.CachedWeather
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherCache
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.data.datastore.weatherCacheKey
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.ui.utils.MockData
import kotlinx.collections.immutable.persistentListOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Test

class WidgetDataTest {

    private val activeCity = WeatherLocal(
        name = "Montreal",
        lat = 45.5,
        lon = -73.55,
        isActive = true,
        isGps = false
    )

    private val weather = Weather(
        currentWeather = MockData.getCurrentWeatherList().first(),
        forecast = MockData.getForecastWeatherList().first()
    )

    private fun cacheWith(lat: Double, lon: Double) = WeatherCache().put(
        weatherCacheKey(lat, lon),
        CachedWeather(weather = weather, fetchedAt = 1L, lang = "en")
    )

    @Test
    fun `active city with cached weather renders name temperature and condition`() {
        val settings = Settings(listWeather = persistentListOf(activeCity))

        val data = buildWidgetData(settings, cacheWith(activeCity.lat, activeCity.lon))

        assertThat(data, `is`(notNullValue()))
        assertThat(data!!.cityName, `is`("Montreal"))
        assertThat(data.temperature, `is`("23°"))
        assertThat(data.description, `is`("cielo claro"))
        assertThat(data.icon, `is`(weather.currentWeather.weatherConditions.icon.weatherIcon))
    }

    @Test
    fun `no active city yields null`() {
        val settings = Settings(
            listWeather = persistentListOf(activeCity.copy(isActive = false))
        )

        assertThat(
            buildWidgetData(settings, cacheWith(activeCity.lat, activeCity.lon)),
            `is`(nullValue())
        )
    }

    @Test
    fun `active city without cache entry yields null`() {
        val settings = Settings(listWeather = persistentListOf(activeCity))

        assertThat(buildWidgetData(settings, cacheWith(0.0, 0.0)), `is`(nullValue()))
    }
}
