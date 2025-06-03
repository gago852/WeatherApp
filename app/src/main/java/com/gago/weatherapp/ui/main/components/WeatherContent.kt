package com.gago.weatherapp.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.ui.WeatherState
import com.gago.weatherapp.ui.main.components.NoWeatherScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherContent(
    state: WeatherState,
    settings: Settings,
    pullState: PullToRefreshState,
    onRefresh: () -> Unit,
    onPermissionRequest: () -> Unit,
    onError: (String) -> Unit
) {
    if (state.weather == null && !settings.permissionAccepted) {
        NoWeatherScreen(onPermissionRequest = onPermissionRequest)
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
