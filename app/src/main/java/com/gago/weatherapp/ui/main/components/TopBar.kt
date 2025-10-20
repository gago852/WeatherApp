package com.gago.weatherapp.ui.main.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.WeatherLocal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopBar(
    activeWeather: WeatherLocal?,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name) +
                        (activeWeather?.let { " - ${it.name}" } ?: "")
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.menu_icon),
                    contentDescription = stringResource(R.string.menu_text_button)
                )
            }
        }
    )
}
