package com.gago.weatherapp.ui.main.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ErrorDisplay(
    error: Int?,
    hasWeather: Boolean,
    onError: (String) -> Unit
) {
    error?.let {
        val errorMessage = stringResource(id = it)
        if (hasWeather) {
            LaunchedEffect(errorMessage) {
                onError(errorMessage)
            }
        } else {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
