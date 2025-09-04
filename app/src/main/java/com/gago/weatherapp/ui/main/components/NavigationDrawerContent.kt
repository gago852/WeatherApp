package com.gago.weatherapp.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import kotlinx.collections.immutable.persistentListOf
import androidx.compose.ui.platform.testTag
import com.google.firebase.crashlytics.FirebaseCrashlytics

@Composable
fun NavigationDrawerContent(
    settings: Settings,
    onSettingsClick: () -> Unit,
    onWeatherItemClick: (Settings) -> Unit,
    onSearchClick: () -> Unit
) {
    ModalDrawerSheet {
        DrawerHeader(onSettingsClick = onSettingsClick, onSearchClick = onSearchClick)
        DrawerItems(settings = settings, onWeatherItemClick = onWeatherItemClick)
    }
}

@Composable
private fun DrawerHeader(onSettingsClick: () -> Unit, onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            modifier = Modifier
                .padding(top = 18.dp)
                .testTag("search_drawer_button"),
            onClick = onSearchClick
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Buscar ciudad"
            )
        }
        IconButton(
            modifier = Modifier
                .padding(top = 18.dp)
                .testTag("settings_drawer_button"),
            onClick = onSettingsClick
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.settings_text)
            )
        }
    }
}

@Composable
private fun DrawerItems(
    settings: Settings,
    onWeatherItemClick: (Settings) -> Unit
) {
    settings.listWeather.forEach { weather ->
        NavigationDrawerItem(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
            label = { Text(text = weather.name) },
            icon = {
                if (weather.isGps) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = stringResource(R.string.from_gps)
                    )
                }
            },
            selected = weather.isActive,
            onClick = {
                if (!weather.isActive) {
                    val updatedSettings = updateActiveWeather(settings, weather)
                    onWeatherItemClick(updatedSettings)
                }
            }
        )
    }
}

private fun updateActiveWeather(settings: Settings, newActiveWeather: WeatherLocal): Settings {
    val currentActive = settings.listWeather.find { it.isActive }

    var tempList = persistentListOf<WeatherLocal>()
    var newList = persistentListOf<WeatherLocal>()
    currentActive?.let {
        val indexTemp = settings.listWeather.indexOf(it)
        tempList = settings.listWeather.set(indexTemp, it.copy(isActive = false))
        val indexActual = settings.listWeather.indexOf(newActiveWeather)
        newList = tempList.set(indexActual, newActiveWeather.copy(isActive = true))
    } ?: run {
        newList = tempList.add(newActiveWeather.copy(isActive = true))
    }

    val crashlytics = FirebaseCrashlytics.getInstance()
    crashlytics.setCustomKey("List size", settings.listWeather.size)
    crashlytics.setCustomKey("Current Active", currentActive?.name ?: "None")
    crashlytics.setCustomKey("New Active", newActiveWeather.toString())
    crashlytics.log("Updating active weather from drawer")
    crashlytics.recordException(Exception("Updating active weather"))

    return settings.copy(listWeather = newList)
}

