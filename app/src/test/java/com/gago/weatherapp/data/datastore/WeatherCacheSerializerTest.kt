package com.gago.weatherapp.data.datastore

import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.model.WeatherTypeIcon
import com.gago.weatherapp.ui.utils.MockData
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.sameInstance
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class WeatherCacheSerializerTest {

    private val weather = Weather(
        currentWeather = MockData.getCurrentWeatherList().first(),
        forecast = MockData.getForecastWeatherList().first()
    )

    @Test
    fun `cache round trips through json`() = runTest {
        val cache = WeatherCache().put(
            weatherCacheKey(10.96, -74.79),
            CachedWeather(weather = weather, fetchedAt = 123L, lang = "es")
        )

        val output = ByteArrayOutputStream()
        WeatherCacheSerializer.writeTo(cache, output)
        val decoded = WeatherCacheSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertThat(decoded, `is`(cache))
    }

    @Test
    fun `icons are persisted by key and resolve back to the same singleton`() = runTest {
        val cache = WeatherCache().put(
            weatherCacheKey(10.96, -74.79),
            CachedWeather(weather = weather, fetchedAt = 123L, lang = "en")
        )

        val output = ByteArrayOutputStream()
        WeatherCacheSerializer.writeTo(cache, output)
        val json = output.toString(Charsets.UTF_8.name())
        // the stable key is persisted, never the drawable resource id
        assertThat(json.contains("clear_sky_night"), `is`(true))

        val decoded = WeatherCacheSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))
        val decodedIcon = decoded.entries.values.first()
            .weather.currentWeather.weatherConditions.icon
        assertThat(decodedIcon, `is`(sameInstance<WeatherTypeIcon>(WeatherTypeIcon.ClearSkyNight)))
    }

    @Test
    fun `weatherCacheKey rounds coordinates so nearby gps fixes share an entry`() {
        assertThat(weatherCacheKey(10.9639, -74.7963), `is`(weatherCacheKey(10.9641, -74.7958)))
        assertThat(
            weatherCacheKey(10.96, -74.79) == weatherCacheKey(11.96, -74.79),
            `is`(false)
        )
    }
}
