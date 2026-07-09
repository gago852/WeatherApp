package com.gago.weatherapp.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.ui.main.states.WeatherState
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.currentLocale
import com.gago.weatherapp.ui.utils.formatFullDateTime


@Composable
fun WeatherContent(
    state: WeatherState,
    settings: Settings,
    pullState: PullToRefreshState,
    onRefresh: () -> Unit,
    onPermissionRequest: () -> Unit,
    onError: (String) -> Unit,
    onShowSearchOverlay: () -> Unit
) {
    if (!state.isLoading && state.weather == null && !settings.permissionAccepted) {
        WelcomeWeatherScreen(
            onPermissionRequest = onPermissionRequest,
            onShowSearchOverlay = onShowSearchOverlay
        )
    } else {
        PullToRefreshBox(
            state = pullState,
            isRefreshing = state.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = if (state.weather == null)
                    Alignment.CenterHorizontally else Alignment.Start,
                verticalArrangement = if (state.weather == null)
                    Arrangement.Center else Arrangement.Top
            ) {
                ErrorDisplay(
                    error = state.error,
                    hasWeather = state.weather != null,
                    onError = onError
                )

                if (state.isFromCache && state.weather != null) {
                    OfflineBanner(lastFetchTime = state.lastFetchTime)
                }

                state.weather?.let {
                    WeatherPresentation(
                        currentWeather = it.currentWeather,
                        fiveDaysForecast = it.forecast,
                        measureUnit = settings.unitOfMeasurement
                    )
                }
            }
        }
    }
}


/** Banner shown above the weather when the data comes from the offline cache. */
@Composable
private fun OfflineBanner(lastFetchTime: Long?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stamp = lastFetchTime?.let {
                formatFullDateTime(it / 1000, currentLocale())
            } ?: ""
            Text(
                text = stringResource(R.string.offline_data_banner, stamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Preview
@Composable
private fun WeatherContentPreview() {
    WeatherAppTheme {
        Surface {
            WeatherContent(
                state = WeatherState(),
                settings = Settings(),
                pullState = rememberPullToRefreshState(),
                onRefresh = {},
                onPermissionRequest = {},
                onError = {},
                onShowSearchOverlay = {}
            )
        }
    }
}