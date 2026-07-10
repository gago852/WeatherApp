package com.gago.weatherapp.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import kotlinx.collections.immutable.persistentListOf

@Composable
fun NavigationDrawerContent(
    settings: Settings,
    onSettingsClick: () -> Unit,
    onWeatherItemClick: (Settings) -> Unit,
    onSearchClick: () -> Unit,
    onRemoveCity: (WeatherLocal) -> Unit = {},
    onReorderCities: (List<WeatherLocal>) -> Unit = {}
) {
    ModalDrawerSheet {
        DrawerHeader(onSettingsClick = onSettingsClick, onSearchClick = onSearchClick)
        DrawerItems(
            settings = settings,
            onWeatherItemClick = onWeatherItemClick,
            onRemoveCity = onRemoveCity,
            onReorderCities = onReorderCities
        )
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
                imageVector = ImageVector.vectorResource(R.drawable.search_icon),
                contentDescription = stringResource(R.string.search_city)
            )
        }
        IconButton(
            modifier = Modifier
                .padding(top = 18.dp)
                .testTag("settings_drawer_button"),
            onClick = onSettingsClick
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.settings_icon),
                contentDescription = stringResource(R.string.settings_text)
            )
        }
    }
}

/**
 * City list with swipe-to-delete (except the GPS city) and long-press drag to reorder.
 * During a drag the reordering happens on a local copy and is committed once on release.
 */
@Composable
private fun DrawerItems(
    settings: Settings,
    onWeatherItemClick: (Settings) -> Unit,
    onRemoveCity: (WeatherLocal) -> Unit,
    onReorderCities: (List<WeatherLocal>) -> Unit
) {
    var draggedList by remember { mutableStateOf<List<WeatherLocal>?>(null) }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var itemHeightPx by remember { mutableIntStateOf(0) }

    val cities = draggedList ?: settings.listWeather

    Column(
        modifier = Modifier.pointerInput(settings.listWeather) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    if (itemHeightPx > 0 && settings.listWeather.isNotEmpty()) {
                        draggedList = settings.listWeather.toList()
                        draggedIndex = (offset.y / itemHeightPx).toInt()
                            .coerceIn(0, settings.listWeather.lastIndex)
                        dragOffset = 0f
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    // each swap must build on the previous one: rebuilding from a stale
                    // snapshot when one event crosses several rows moves the wrong item
                    var current = draggedList ?: return@detectDragGesturesAfterLongPress
                    dragOffset += dragAmount.y
                    while (dragOffset > itemHeightPx / 2f && draggedIndex < current.lastIndex) {
                        current = current.toMutableList().apply {
                            add(draggedIndex + 1, removeAt(draggedIndex))
                        }
                        draggedIndex++
                        dragOffset -= itemHeightPx
                    }
                    while (dragOffset < -itemHeightPx / 2f && draggedIndex > 0) {
                        current = current.toMutableList().apply {
                            add(draggedIndex - 1, removeAt(draggedIndex))
                        }
                        draggedIndex--
                        dragOffset += itemHeightPx
                    }
                    draggedList = current
                },
                onDragEnd = {
                    draggedList?.let { onReorderCities(it) }
                    draggedList = null
                    draggedIndex = -1
                },
                onDragCancel = {
                    draggedList = null
                    draggedIndex = -1
                }
            )
        }
    ) {
        cities.forEachIndexed { index, weather ->
            val isDragged = draggedList != null && index == draggedIndex
            Box(
                modifier = Modifier
                    .onSizeChanged { if (itemHeightPx == 0) itemHeightPx = it.height }
                    .graphicsLayer {
                        translationY = if (isDragged) dragOffset else 0f
                        shadowElevation = if (isDragged) 8f else 0f
                    }
            ) {
                RemovableDrawerItem(
                    weather = weather,
                    settings = settings,
                    onWeatherItemClick = onWeatherItemClick,
                    onRemoveCity = onRemoveCity
                )
            }
        }
    }
}

@Composable
private fun RemovableDrawerItem(
    weather: WeatherLocal,
    settings: Settings,
    onWeatherItemClick: (Settings) -> Unit,
    onRemoveCity: (WeatherLocal) -> Unit
) {
    val item = @Composable {
        NavigationDrawerItem(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
            label = { Text(text = weather.name) },
            icon = {
                if (weather.isGps) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.location_icon),
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

    // The GPS city cannot be removed, only deactivated by picking another city.
    if (weather.isGps) {
        item()
        return
    }

    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onRemoveCity(weather)
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        // the drawer item has a transparent container, so the background must only be
        // drawn while the item is actually displaced by a swipe
        backgroundContent = {
            val swiping = runCatching { dismissState.requireOffset() != 0f }.getOrDefault(false)
            if (!swiping) return@SwipeToDismissBox
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.delete),
                    contentDescription = stringResource(R.string.remove_city),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    ) {
        item()
    }
}

private fun updateActiveWeather(settings: Settings, newActiveWeather: WeatherLocal): Settings {
    val currentActive = settings.listWeather.find { it.isActive }

    var tempList = persistentListOf<WeatherLocal>()
    var newList = persistentListOf<WeatherLocal>()
    currentActive?.let {
        val indexTemp = settings.listWeather.indexOf(it)
        tempList = settings.listWeather.replacingAt(indexTemp, it.copy(isActive = false))
        val indexActual = settings.listWeather.indexOf(newActiveWeather)
        newList = tempList.replacingAt(indexActual, newActiveWeather.copy(isActive = true))
    } ?: run {
        newList = tempList.adding(newActiveWeather.copy(isActive = true))
    }

    return settings.copy(listWeather = newList)
}
