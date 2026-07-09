package com.gago.weatherapp.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gago.weatherapp.R
import com.gago.weatherapp.domain.model.WeatherForecast
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.MockData
import com.gago.weatherapp.ui.utils.currentLocale
import com.gago.weatherapp.ui.utils.formatShortHour
import kotlin.math.roundToInt

/** Horizontally scrollable next-24h row: one card per 3-hour slot. */
@Composable
fun HourlyForecastRow(
    hourlyForecast: List<WeatherForecast>,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier.fillMaxWidth()) {
        items(hourlyForecast) { slot ->
            HourlyForecastItem(slot = slot)
        }
    }
}

@Composable
private fun HourlyForecastItem(slot: WeatherForecast) {
    val hour = formatShortHour(
        epochSeconds = slot.forecastTime,
        timeZoneOffset = slot.timeZoneOffset,
        locale = currentLocale()
    )
    val temperature = "${slot.mainData.temp.roundToInt()}°"
    val precipitation = "${(slot.probabilityOfPrecipitation * 100).roundToInt()}%"
    val slotDescription = stringResource(
        R.string.hourly_slot_content_description,
        hour, temperature, slot.weatherCondition.description, precipitation
    )

    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .semantics { contentDescription = slotDescription },
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = hour, style = MaterialTheme.typography.labelMedium)
            Image(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = slot.weatherCondition.icon.weatherIcon),
                contentDescription = null
            )
            Text(text = temperature, fontSize = 18.sp)
            Text(
                text = precipitation,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun previewHourly(): List<WeatherForecast> {
    val base = MockData.getWeatherForecast()
    return (0 until 8).map { index ->
        base.copy(
            forecastTime = index * 3 * 3_600L,
            timeZoneOffset = 0L,
            mainData = base.mainData.copy(temp = 20.0 + index),
            probabilityOfPrecipitation = index / 10.0
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HourlyForecastRowPreview() {
    WeatherAppTheme {
        Surface {
            HourlyForecastRow(hourlyForecast = previewHourly())
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun HourlyForecastRowDarkPreview() {
    WeatherAppTheme {
        Surface {
            HourlyForecastRow(hourlyForecast = previewHourly())
        }
    }
}
