package com.gago.weatherapp.ui.main.components

import com.gago.weatherapp.domain.model.WeatherForecast
import com.gago.weatherapp.ui.utils.MockData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.`is`
import org.junit.Test

class TemperatureChartDataTest {

    private fun forecast(day: String, temp: Double, description: String = "cielo claro"): WeatherForecast {
        val base = MockData.getWeatherForecast()
        return base.copy(
            calculatedTime = day,
            mainData = base.mainData.copy(temp = temp),
            weatherCondition = base.weatherCondition.copy(description = description)
        )
    }

    @Test
    fun toTemperatureChartPoints_mapsDayTemperatureAndDescription() {
        val forecasts = listOf(
            forecast("Monday", 22.4, "clear sky"),
            forecast("Tuesday", 26.1, "light rain")
        )

        val points = forecasts.toTemperatureChartPoints()

        assertThat(points.size, `is`(2))
        assertThat(points[0].label, `is`("Monday"))
        assertThat(points[0].shortLabel, `is`("Mon"))
        assertThat(points[0].temperature, `is`(22.4))
        assertThat(points[0].description, `is`("clear sky"))
        assertThat(points[1].label, `is`("Tuesday"))
        assertThat(points[1].temperature, `is`(26.1))
        assertThat(points[1].description, `is`("light rain"))
    }

    @Test
    fun toTemperatureChartPoints_emptyForecast_returnsEmptyList() {
        assertThat(emptyList<WeatherForecast>().toTemperatureChartPoints(), `is`(empty()))
    }

    @Test
    fun temperatureToY_minTemperatureIsDrawnAtTheBottom() {
        val y = temperatureToY(temperature = 10.0, minTemperature = 10.0, maxTemperature = 30.0, height = 100f)

        assertThat(y, `is`(100f))
    }

    @Test
    fun temperatureToY_maxTemperatureIsDrawnAtTheTop() {
        val y = temperatureToY(temperature = 30.0, minTemperature = 10.0, maxTemperature = 30.0, height = 100f)

        assertThat(y, `is`(0f))
    }

    @Test
    fun temperatureToY_middleTemperatureIsCentered() {
        val y = temperatureToY(temperature = 20.0, minTemperature = 10.0, maxTemperature = 30.0, height = 100f)

        assertThat(y, `is`(50f))
    }

    @Test
    fun temperatureToY_flatSeriesIsCentered() {
        val y = temperatureToY(temperature = 25.0, minTemperature = 25.0, maxTemperature = 25.0, height = 100f)

        assertThat(y, `is`(50f))
    }

    @Test
    fun nearestPointIndex_picksTheClosestPoint() {
        // 5 points starting at x=20 with a step of 100: 20, 120, 220, 320, 420
        assertThat(nearestPointIndex(tapX = 20f, firstX = 20f, stepX = 100f, count = 5), `is`(0))
        assertThat(nearestPointIndex(tapX = 165f, firstX = 20f, stepX = 100f, count = 5), `is`(1))
        assertThat(nearestPointIndex(tapX = 180f, firstX = 20f, stepX = 100f, count = 5), `is`(2))
        assertThat(nearestPointIndex(tapX = 419f, firstX = 20f, stepX = 100f, count = 5), `is`(4))
    }

    @Test
    fun nearestPointIndex_clampsOutOfRangeTaps() {
        assertThat(nearestPointIndex(tapX = -50f, firstX = 20f, stepX = 100f, count = 5), `is`(0))
        assertThat(nearestPointIndex(tapX = 9_999f, firstX = 20f, stepX = 100f, count = 5), `is`(4))
    }

    @Test
    fun nearestPointIndex_singlePointAlwaysReturnsZero() {
        assertThat(nearestPointIndex(tapX = 500f, firstX = 20f, stepX = 0f, count = 1), `is`(0))
    }
}
