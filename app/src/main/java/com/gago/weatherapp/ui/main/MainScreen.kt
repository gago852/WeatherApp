package com.gago.weatherapp.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.ui.main.states.WeatherState
import com.gago.weatherapp.ui.main.viewModels.WeatherViewModel
import com.gago.weatherapp.ui.main.components.HandlePermissionDialogs
import com.gago.weatherapp.ui.main.components.NavigationDrawerContent
import com.gago.weatherapp.ui.main.components.WeatherContent
import com.gago.weatherapp.ui.main.components.WeatherTopBar
import com.gago.weatherapp.ui.main.utils.handleRefresh
import com.gago.weatherapp.ui.navigation.AppScreens
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.ReasonsForRefresh
import kotlinx.coroutines.launch
import com.gago.weatherapp.ui.main.viewModels.SearchCityViewModel
import com.gago.weatherapp.ui.main.states.SearchCityUiState
import com.gago.weatherapp.ui.main.components.SearchCityOverlay
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.gago.weatherapp.domain.model.GeoCoordinate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    searchCityViewModel: SearchCityViewModel = hiltViewModel()
) {
    val isSetup = weatherViewModel.isStartup.collectAsState().value
    val state = weatherViewModel.state
    val mainScope = rememberCoroutineScope()

//    val settingValue =  if (isSetup) Settings() else weatherViewModel.settings.collectAsState(initial = Settings()).value
    val settingValue = weatherViewModel.settings.collectAsState(initial = Settings()).value

    val locationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            weatherViewModel.onPermissionResult(
                permission = Manifest.permission.ACCESS_COARSE_LOCATION,
                isGranted = isGranted
            )
            mainScope.launch {
                weatherViewModel.setPermissionAccepted(isGranted)
                if (isGranted) {
                    weatherViewModel.setReasonForRefresh(ReasonsForRefresh.PULL)
                    weatherViewModel.loadLocationWeather()
                }
            }
        }
    )

    val searchUiState = searchCityViewModel.uiState.collectAsState().value
    val selectedGeo = searchCityViewModel.selectedGeoCoordinate.collectAsState().value

    LaunchedEffect(selectedGeo) {
        selectedGeo?.let { geo: GeoCoordinate ->
            try {
                // Llamar al WeatherViewModel para actualizar el clima con las nuevas coordenadas
                weatherViewModel.loadWeatherFromSearch(
                    latitude = geo.latitude,
                    longitude = geo.longitude,
                    cityName = geo.name ?: "Unknown City"
                )
            } finally {
                // Resetear completamente el overlay con valores por defecto
                searchCityViewModel.onDismiss()
                searchCityViewModel.resetSelectedGeoCoordinate()
            }
        }
    }

    LaunchedEffect(isSetup) {
        if (isSetup) {
            weatherViewModel.setReasonForRefresh(ReasonsForRefresh.STARTUP)
            handleRefresh(
                weatherViewModel = weatherViewModel,
                locationPermissionResultLauncher = locationPermissionResultLauncher
            )
        }
    }

    HandlePermissionDialogs(
        dialogQueue = weatherViewModel.visiblePermissionDialogQueue,
        onDismiss = weatherViewModel::dismissDialog,
        onPermissionRequest = { permission ->
            weatherViewModel.dismissDialog()
            locationPermissionResultLauncher.launch(permission)
        },
        onGoToSettings = { context ->
            weatherViewModel.dismissDialog()
            openAppSettings(context)
        }
    )

    LifecycleResumeEffect(true) {
        if (weatherViewModel.wentToSettings) {
            weatherViewModel.setReasonForRefresh(ReasonsForRefresh.PULL)
            weatherViewModel.refreshWeather()
            weatherViewModel.setWentToSettings(false)
        }
        onPauseOrDispose {}
    }


    WeatherNavDrawer(
        settingValue = settingValue,
        state = state,
        onSettingsClick = {
            weatherViewModel.setWentToSettings(true)
            navController.navigate(AppScreens.SettingScreen.route)
        },
        onWeatherItemClick = { newSettings ->
            weatherViewModel.setSettingChanged(newSettings)
            weatherViewModel.setReasonForRefresh(ReasonsForRefresh.WEATHER_CHANGED)
            mainScope.launch {
                handleRefresh(
                    weatherViewModel = weatherViewModel,
                    locationPermissionResultLauncher = locationPermissionResultLauncher
                )
            }
        },
        onRefresh = {
            mainScope.launch {
                handleRefresh(
                    weatherViewModel = weatherViewModel,
                    locationPermissionResultLauncher = locationPermissionResultLauncher
                )
            }
        },
        onPermissionRequest = {
            locationPermissionResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        },
        searchUiState = searchUiState,
        onShowSearchOverlay = { searchCityViewModel.showOverlay() },
        onDismissSearchOverlay = { searchCityViewModel.onDismiss() },
        onSearchTextChanged = { searchCityViewModel.onSearchTextChanged(it) },
        onResultClick = { searchCityViewModel.onResultClick(it) },
        onClearSearch = { searchCityViewModel.onClear() },
        showAddGpsButton = settingValue.listWeather.none { it.isGps },
        onAddGpsCity = {
            if (settingValue.permissionAccepted) {
                mainScope.launch { weatherViewModel.loadLocationWeather() }
                searchCityViewModel.onDismiss()
            } else {
                locationPermissionResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                searchCityViewModel.onDismiss()
            }
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherNavDrawer(
    settingValue: Settings,
    state: WeatherState,
    onSettingsClick: () -> Unit,
    onWeatherItemClick: (Settings) -> Unit,
    onRefresh: () -> Unit,
    onPermissionRequest: () -> Unit,
    searchUiState: SearchCityUiState,
    onShowSearchOverlay: () -> Unit,
    onDismissSearchOverlay: () -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onResultClick: (AutocompletePrediction) -> Unit,
    onClearSearch: () -> Unit,
    showAddGpsButton: Boolean,
    onAddGpsCity: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val pullState = rememberPullToRefreshState()
    val navScope = rememberCoroutineScope()
    BackHandler(enabled = drawerState.isOpen) {
        navScope.launch { drawerState.close() }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                settings = settingValue,
                onSettingsClick = {
                    onSettingsClick()
                    navScope.launch { drawerState.close() }
                },
                onWeatherItemClick = { newSettings ->
                    onWeatherItemClick(newSettings)
                    navScope.launch { drawerState.close() }
                },
                onSearchClick = {
                    onShowSearchOverlay()
                    navScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
            topBar = {
                WeatherTopBar(
                    activeWeather = settingValue.listWeather.find { it.isActive },
                    onMenuClick = { navScope.launch { drawerState.open() } }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
                contentAlignment = if (state.weather == null && !settingValue.permissionAccepted)
                    Alignment.Center else Alignment.TopStart
            ) {
                WeatherContent(
                    state = state,
                    settings = settingValue,
                    pullState = pullState,
                    onRefresh = onRefresh,
                    onPermissionRequest = onPermissionRequest,
                    onError = { error -> navScope.launch { snackBarHostState.showSnackbar(error) } },
                    onShowSearchOverlay = onShowSearchOverlay
                )
                if (searchUiState.isVisible) {
                    SearchCityOverlay(
                        isVisible = searchUiState.isVisible,
                        onDismiss = { onDismissSearchOverlay() },
                        searchResults = searchUiState.searchResults,
                        onSearchTextChanged = { onSearchTextChanged(it) },
                        searchText = searchUiState.searchText,
                        onResultClick = { onResultClick(it) },
                        isLoading = searchUiState.isLoading,
                        error = searchUiState.error,
                        onClear = { onClearSearch() },
                        showAddGpsButton = showAddGpsButton,
                        onAddGpsCity = onAddGpsCity
                    )
                }
            }
        }
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    WeatherAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WeatherNavDrawer(
                settingValue = Settings(),
                state = WeatherState(isLoading = false),
                onSettingsClick = {},
                onWeatherItemClick = {},
                onRefresh = {},
                onPermissionRequest = {},
                searchUiState = SearchCityUiState(),
                onShowSearchOverlay = {},
                onDismissSearchOverlay = {},
                onSearchTextChanged = {},
                onResultClick = {},
                onClearSearch = {},
                showAddGpsButton = false,
                onAddGpsCity = {}
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun MainScreenDarkPreview() {
    WeatherAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WeatherNavDrawer(
                settingValue = Settings(),
                state = WeatherState(isLoading = false),
                onSettingsClick = {},
                onWeatherItemClick = {},
                onRefresh = {},
                onPermissionRequest = {},
                searchUiState = SearchCityUiState(),
                onShowSearchOverlay = {},
                onDismissSearchOverlay = {},
                onSearchTextChanged = {},
                onResultClick = {},
                onClearSearch = {},
                showAddGpsButton = false,
                onAddGpsCity = {}
            )
        }
    }
}