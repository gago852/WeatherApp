package com.gago.weatherapp.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gago.weatherapp.domain.model.WeatherForecast
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.MockData
import com.gago.weatherapp.ui.utils.capitalizeWords
import kotlin.math.roundToInt

@Composable
fun ForecastItem(weatherForecast: WeatherForecast) {
    ElevatedCard(modifier = Modifier.padding(8.dp), colors = CardDefaults.elevatedCardColors()) {

        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = weatherForecast.calculatedTime)
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