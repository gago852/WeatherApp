package com.gago.weatherapp.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gago.weatherapp.R
import com.gago.weatherapp.domain.model.WeatherForecast
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.MockData
import com.gago.weatherapp.ui.utils.capitalizeWords
import com.gago.weatherapp.ui.utils.currentLocale
import com.gago.weatherapp.ui.utils.formatDayOfWeek
import kotlin.math.roundToInt

@Composable
fun ForecastItem(weatherForecast: WeatherForecast) {
    ElevatedCard(modifier = Modifier.padding(8.dp), colors = CardDefaults.elevatedCardColors()) {

        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatDayOfWeek(
                    epochSeconds = weatherForecast.forecastTime,
                    timeZoneOffset = weatherForecast.timeZoneOffset,
                    locale = currentLocale()
                )
            )
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(80.dp),
                    painter = painterResource(id = weatherForecast.weatherCondition.icon.weatherIcon),
                    contentDescription = weatherForecast.weatherCondition.description
                )
                Text(
                    text = weatherForecast.mainData.temp.roundToInt().toString().plus("°"),
                    fontSize = 30.sp
                )
            }

            Text(text = weatherForecast.weatherCondition.description.capitalizeWords())
            Text(
                text = stringResource(
                    R.string.precipitation_probability_text,
                    (weatherForecast.probabilityOfPrecipitation * 100).roundToInt()
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

    }
}

@Preview(
    showBackground = true, showSystemUi = false
)
@Composable
private fun ForecastItemPreviewLight() {
    WeatherAppTheme {
        Surface() {
            ForecastItem(weatherForecast = MockData.getWeatherForecast())
        }
    }
}

@Preview(
    showBackground = true, showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun ForecastItemPreviewDark() {
    WeatherAppTheme {
        Surface() {
            ForecastItem(weatherForecast = MockData.getWeatherForecast())
        }
    }
}