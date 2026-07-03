package com.gago.weatherapp.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gago.weatherapp.R
import com.gago.weatherapp.domain.model.WeatherForecast
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.MeasureUnit
import com.gago.weatherapp.ui.utils.MockData
import com.gago.weatherapp.ui.utils.capitalizeWords
import kotlin.math.roundToInt

@Composable
fun TemperatureChart(
    forecast: List<WeatherForecast>,
    measureUnit: MeasureUnit,
    modifier: Modifier = Modifier
) {
    val points = remember(forecast) { forecast.toTemperatureChartPoints() }
    if (points.size < 2) return

    var selectedIndex by rememberSaveable(forecast) { mutableIntStateOf(-1) }

    val lineColor = MaterialTheme.colorScheme.primary
    val selectedColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val textMeasurer = rememberTextMeasurer()

    val unitText = measureUnit.tempUnitText
    val chartDescription = stringResource(R.string.temperature_chart_content_description)
        .plus(" ")
        .plus(points.joinToString { point ->
            "${point.label}: ${point.temperature.roundToInt()} $unitText"
        })

    ElevatedCard(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.temperature_trend_text),
                style = MaterialTheme.typography.titleMedium
            )
            val selectedPoint = points.getOrNull(selectedIndex)
            Text(
                text = selectedPoint?.let { point ->
                    "${point.label.capitalizeWords()} · " +
                            "${point.temperature.roundToInt()}$unitText · " +
                            point.description.capitalizeWords()
                } ?: stringResource(R.string.temperature_chart_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("temperature_chart_detail")
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
                    .testTag("temperature_chart")
                    .semantics { contentDescription = chartDescription }
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val sidePadding = 20.dp.toPx()
                            val stepX = (size.width - 2 * sidePadding) / (points.size - 1)
                            selectedIndex =
                                nearestPointIndex(offset.x, sidePadding, stepX, points.size)
                        }
                    }
            ) {
                val sidePadding = 20.dp.toPx()
                val topPadding = 24.dp.toPx()
                val bottomLabelSpace = 24.dp.toPx()
                val chartHeight = size.height - topPadding - bottomLabelSpace
                val stepX = (size.width - 2 * sidePadding) / (points.size - 1)

                val minTemp = points.minOf { it.temperature }
                val maxTemp = points.maxOf { it.temperature }
                val offsets = points.mapIndexed { index, point ->
                    Offset(
                        x = sidePadding + index * stepX,
                        y = topPadding + temperatureToY(
                            point.temperature, minTemp, maxTemp, chartHeight
                        )
                    )
                }

                // Horizontal grid lines (top / middle / bottom of the chart area)
                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = topPadding + chartHeight * fraction
                    drawLine(
                        color = gridColor,
                        start = Offset(sidePadding, y),
                        end = Offset(size.width - sidePadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Soft gradient fill under the line
                val fillPath = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    offsets.drop(1).forEach { lineTo(it.x, it.y) }
                    lineTo(offsets.last().x, topPadding + chartHeight)
                    lineTo(offsets.first().x, topPadding + chartHeight)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0f)),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                // Trend line
                val linePath = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    offsets.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                offsets.forEachIndexed { index, offset ->
                    val isSelected = index == selectedIndex

                    drawCircle(
                        color = if (isSelected) selectedColor else lineColor,
                        radius = (if (isSelected) 6.dp else 4.dp).toPx(),
                        center = offset
                    )

                    // Temperature value above each point
                    val tempLayout = textMeasurer.measure(
                        text = "${points[index].temperature.roundToInt()}°",
                        style = labelStyle
                    )
                    drawText(
                        textLayoutResult = tempLayout,
                        topLeft = Offset(
                            x = offset.x - tempLayout.size.width / 2f,
                            y = (offset.y - tempLayout.size.height - 6.dp.toPx())
                                .coerceAtLeast(0f)
                        )
                    )

                    // Day label under the chart area
                    val dayLayout = textMeasurer.measure(
                        text = points[index].shortLabel.capitalizeWords(),
                        style = labelStyle
                    )
                    drawText(
                        textLayoutResult = dayLayout,
                        topLeft = Offset(
                            x = offset.x - dayLayout.size.width / 2f,
                            y = topPadding + chartHeight + 6.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}

private fun previewForecast(): List<WeatherForecast> {
    val base = MockData.getWeatherForecast()
    return listOf("Lunes" to 22.4, "Martes" to 26.1, "Miércoles" to 24.0, "Jueves" to 28.7, "Viernes" to 25.3)
        .map { (day, temp) ->
            base.copy(calculatedTime = day, mainData = base.mainData.copy(temp = temp))
        }
}

@Preview(showBackground = true)
@Composable
private fun TemperatureChartPreview() {
    WeatherAppTheme {
        Surface {
            TemperatureChart(
                forecast = previewForecast(),
                measureUnit = MeasureUnit.METRIC
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun TemperatureChartDarkPreview() {
    WeatherAppTheme {
        Surface {
            TemperatureChart(
                forecast = previewForecast(),
                measureUnit = MeasureUnit.METRIC
            )
        }
    }
}
