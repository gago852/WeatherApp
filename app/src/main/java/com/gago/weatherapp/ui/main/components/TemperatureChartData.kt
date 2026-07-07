package com.gago.weatherapp.ui.main.components

import com.gago.weatherapp.domain.model.WeatherForecast
import kotlin.math.roundToInt

/** One tappable point of the temperature trend chart. */
data class TemperatureChartPoint(
    val label: String,
    val shortLabel: String,
    val temperature: Double,
    val description: String
)

/** Maps the daily forecast list to chart points (X label = day, Y = temperature). */
fun List<WeatherForecast>.toTemperatureChartPoints(): List<TemperatureChartPoint> {
    return map { forecast ->
        TemperatureChartPoint(
            label = forecast.calculatedTime,
            shortLabel = forecast.calculatedTime.take(3),
            temperature = forecast.mainData.temp,
            description = forecast.weatherCondition.description
        )
    }
}

/**
 * Maps a temperature to a Y offset inside the chart area: the maximum temperature is drawn
 * at the top (0) and the minimum at the bottom (height). A flat series is centered.
 */
fun temperatureToY(
    temperature: Double,
    minTemperature: Double,
    maxTemperature: Double,
    height: Float
): Float {
    if (maxTemperature == minTemperature) return height / 2f
    val fraction = (temperature - minTemperature) / (maxTemperature - minTemperature)
    return (height * (1f - fraction)).toFloat()
}

/** Index of the chart point whose X position is closest to a tap at [tapX]. */
fun nearestPointIndex(tapX: Float, firstX: Float, stepX: Float, count: Int): Int {
    if (count <= 1 || stepX <= 0f) return 0
    val index = ((tapX - firstX) / stepX).roundToInt()
    return index.coerceIn(0, count - 1)
}
