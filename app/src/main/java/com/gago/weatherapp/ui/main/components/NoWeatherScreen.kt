package com.gago.weatherapp.ui.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gago.weatherapp.R
import com.gago.weatherapp.ui.theme.WeatherAppTheme

@Composable
fun NoWeatherScreen(onPermissionRequest: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_weather_text),
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onPermissionRequest,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = stringResource(R.string.grant_permission))
        }
    }
}

@Preview
@Composable
private fun NoWeatherPreview() {
    WeatherAppTheme {
        Surface {
            NoWeatherScreen(onPermissionRequest = {})
        }
    }
}
