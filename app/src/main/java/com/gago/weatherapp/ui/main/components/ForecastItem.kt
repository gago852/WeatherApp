package com.gago.weatherapp.ui.main.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gago.weatherapp.domain.model.WeatherForecast

@Composable
fun ForecastItem(weatherForecast: WeatherForecast) {

}

@Preview(
    showBackground = true, showSystemUi = false
)
@Composable
private fun ForecastItemPreviewLight() {

}

@Preview(
    showBackground = true, showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun ForecastItemPreviewDark() {

}