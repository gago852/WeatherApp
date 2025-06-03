package com.gago.weatherapp.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.ui.WeatherState
import com.gago.weatherapp.ui.WeatherViewModel
import com.gago.weatherapp.ui.main.components.ErrorDisplay
import com.gago.weatherapp.ui.main.components.HandlePermissionDialogs
import com.gago.weatherapp.ui.main.components.NavigationDrawerContent
import com.gago.weatherapp.ui.main.components.PermissionDialog
import com.gago.weatherapp.ui.main.components.WeatherContent
import com.gago.weatherapp.ui.main.components.WeatherTopBar
import com.gago.weatherapp.ui.main.utils.handleRefresh
import com.gago.weatherapp.ui.navigation.AppScreens
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.ReasonsForRefresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val permissionsToRequest = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    val isSetup = weatherViewModel.isStartup.collectAsState().value
    val state = weatherViewModel.state
    val mainScope = rememberCoroutineScope()


    val settingValue =
        if (isSetup) Settings() else weatherViewModel.settings.collectAsState(initial = Settings()).value

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
                    weatherViewModel.refreshWeather()
                }
            }
        }
    )

    LaunchedEffect(isSetup) {
        if (isSetup) {
            val setting = weatherViewModel.getInitialSetUp()
            setting?.let {
                val listWeatherStoredActive = it.listWeather.filter { lit -> lit.isActive }
                val weatherCurrent = listWeatherStoredActive.firstOrNull()
                weatherCurrent?.let { weatherLocal ->
                    if (weatherLocal.isGps) {
                        locationPermissionResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    } else {
                        weatherViewModel.loadWeatherFromCurrent(weatherLocal)
                    }
                }
            }
            weatherViewModel.initialStartUp()
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
            weatherViewModel.refreshWeather()
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
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier,
            contentWindowInsets = WindowInsets.safeDrawing,
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
                    .padding(paddingValues),
                contentAlignment = if (state.weather == null && !settingValue.permissionAccepted)
                    Alignment.Center else Alignment.TopStart
            ) {
                WeatherContent(
                    state = state,
                    settings = settingValue,
                    pullState = pullState,
                    onRefresh = onRefresh,
                    onPermissionRequest = onPermissionRequest,
                    onError = { error -> navScope.launch { snackBarHostState.showSnackbar(error) } }
                )
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

@Preview
@Composable
fun MainScreenPreview() {
    WeatherAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WeatherNavDrawer(
                settingValue = Settings(),
                state = WeatherState(),
                onSettingsClick = {},
                onWeatherItemClick = {},
                onRefresh = {},
                onPermissionRequest = {}
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenDarkPreview() {
    WeatherAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WeatherNavDrawer(
                settingValue = Settings(),
                state = WeatherState(),
                onSettingsClick = {},
                onWeatherItemClick = {},
                onRefresh = {},
                onPermissionRequest = {}
            )
        }
    }
}