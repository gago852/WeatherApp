package com.gago.weatherapp.ui.main

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.ui.WeatherState
import com.gago.weatherapp.ui.WeatherViewModel
import com.gago.weatherapp.ui.main.components.AccessCoarseLocationPermissionTextProvider
import com.gago.weatherapp.ui.main.components.PermissionDialog
import com.gago.weatherapp.ui.main.components.WeatherPresentation
import com.gago.weatherapp.ui.navigation.AppScreens
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.ReasonsForRefresh
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshState

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
    val pullState = rememberPullToRefreshState()

    val settingValue =
        if (isSetup) Settings() else weatherViewModel.settings.collectAsState(initial = Settings()).value

    val locationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            run {
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

    val dialogQueue = weatherViewModel.visiblePermissionDialogQueue
//    Log.d("StateOnMainScreen", state.toString())
//    Log.d("StateOnMainScreenSettings", settingValue.toString())

    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsToRequest.forEach { permission ->
                weatherViewModel.onPermissionResult(
                    permission = permission,
                    isGranted = perms[permission] == true
                )
            }
        }
    )

    val context = LocalContext.current

    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.ACCESS_COARSE_LOCATION -> {
                        AccessCoarseLocationPermissionTextProvider()
                    }

                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                    context as Activity,
                    permission
                ),
                onDismiss = weatherViewModel::dismissDialog,
                onOkClick = {
                    weatherViewModel.dismissDialog()
                    multiplePermissionResultLauncher.launch(
                        arrayOf(permission)
                    )
                },
                onGoToAppSettingsClick = {
                    weatherViewModel.dismissDialog()
                    openAppSettings(context)
                }
            )
        }

    LifecycleResumeEffect(true) {
        Log.d("LifecycleResumeEffect", "resume ${weatherViewModel.wentToSettings}")
        if (weatherViewModel.wentToSettings) {
            weatherViewModel.setReasonForRefresh(ReasonsForRefresh.PULL)
            weatherViewModel.refreshWeather()
            weatherViewModel.setWentToSettings(false)
        }
        onPauseOrDispose {
            Log.d("LifecycleResumeEffect", "pause $weatherViewModel.wentToSettings")
        }
    }

    NavDrawerMainScreen(
        state = state,
        settings = settingValue,
        navController = navController,
        onPermissionRequest = {
            locationPermissionResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        },
        onSettingsButtonPress = {
            weatherViewModel.setWentToSettings(true)
        },
        pullState = pullState,
        onRefresh = {
            mainScope.launch {
                when (weatherViewModel.reasonForRefresh) {
                    ReasonsForRefresh.WEATHER_CHANGED -> {
                        val settingChanged = weatherViewModel.settingChanged?.copy()
                        settingChanged?.let {
                            weatherViewModel.loadAnotherWeather(it)
                            weatherViewModel.setSettingChanged(null)
                        }
                        weatherViewModel.setReasonForRefresh(ReasonsForRefresh.PULL)
                    }

                    ReasonsForRefresh.STARTUP -> {
                        val setting = weatherViewModel.getInitialSetUp()
                        setting?.let {
                            val listWeatherStoredActive =
                                it.listWeather.filter { lit -> lit.isActive }
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

                    else -> weatherViewModel.refreshWeather()
                }
            }
        },
        onActiveWeatherChanged = {
            weatherViewModel.setSettingChanged(it)
            weatherViewModel.setReasonForRefresh(ReasonsForRefresh.WEATHER_CHANGED)
            weatherViewModel.refreshWeather()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerMainScreen(
    state: WeatherState,
    settings: Settings,
    navController: NavController,
    pullState: PullToRefreshState,
    onPermissionRequest: () -> Unit,
    onSettingsButtonPress: () -> Unit,
    onRefresh: () -> Unit,
    onActiveWeatherChanged: (Settings) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 28.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = Modifier.padding(top = 18.dp),
                        onClick = {
                            onSettingsButtonPress()
                            navController.navigate(AppScreens.SettingScreen.route)
                            scope.launch {
                                drawerState.close()
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(
                                R.string.settings_text
                            )
                        )
                    }
                }
                settings.listWeather.forEach { weather ->
                    NavigationDrawerItem(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                        label = {
                            Text(text = weather.name)
                        },
                        icon = {
                            if (weather.isGps) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = stringResource(R.string.from_gps)
                                )
                            }
                        },
                        selected = weather.isActive, onClick = {
                            if (!weather.isActive) {
                                val tempActivated = settings.listWeather.find {
                                    it.isActive
                                }

                                var tempList = persistentListOf<WeatherLocal>()

                                tempActivated?.let {
                                    val indexTemp = settings.listWeather.indexOf(it)
                                    tempList =
                                        settings.listWeather.set(
                                            indexTemp,
                                            it.copy(isActive = false)
                                        )
                                }

                                val indexActual = settings.listWeather.indexOf(weather)
                                val newList =
                                    tempList.set(indexActual, weather.copy(isActive = true))

                                val newSettings = settings.copy(
                                    listWeather = newList
                                )

                                onActiveWeatherChanged(newSettings)
                            } else {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                        })
                }

            }


        }) {

        Scaffold(
            modifier = Modifier,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                val activeWeather = settings.listWeather.find { it.isActive }?.let {
                    "- ${it.name}"
                } ?: ""

                TopAppBar(

                    title = { Text(text = stringResource(id = R.string.app_name) + " $activeWeather") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.menu_text_button)
                            )
                        }
                    }
                )
            },
            content = { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
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
                                horizontalAlignment = if (state.weather == null) Alignment.CenterHorizontally else Alignment.Start,
                                verticalArrangement = if (state.weather == null) Arrangement.Center else Arrangement.Top
                            ) {
                                state.error?.let {
                                    val error = stringResource(id = it)
                                    if (state.weather != null) {
                                        LaunchedEffect(error) {
                                            snackbarHostState.showSnackbar(error)
                                        }
                                    } else {
                                        Text(text = error, modifier = Modifier.padding(16.dp))
                                    }
                                }

                                state.weather?.let {
                                    WeatherPresentation(
                                        currentWeather = it.currentWeather,
                                        fiveDaysForecast = it.forecast,
                                        measureUnit = settings.unitOfMeasurement
                                    )
                                }

                                when {
                                    state.isLoading -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }

                                    state.error != null -> {
                                        val error = stringResource(id = state.error)
                                        if (state.weather != null) {
                                            LaunchedEffect(error) {
                                                snackbarHostState.showSnackbar(error)
                                            }
                                        } else {
                                            Text(text = error, modifier = Modifier.padding(16.dp))
                                        }
                                    }

                                    state.weather == null -> {
                                        Text(text = stringResource(R.string.swipe_to_load_text))

                                    }
                                }
                            }
                        }
                    }

                }
            }
        )
    }
}

@Composable
fun NoWeatherScreen(onPermissionRequest: () -> Unit) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    Card(modifier = Modifier.padding(32.dp)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                text = stringResource(R.string.welcome_title)
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth(if (screenWidth > 500.dp) 0.4f else 1.0f)
                    .padding(start = 16.dp, end = 16.dp),
                text = stringResource(R.string.welcome_message)
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Button(onClick = { onPermissionRequest() }) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = stringResource(R.string.button_get_gps)
                )
                Text(text = stringResource(R.string.button_get_gps))
            }
            Spacer(modifier = Modifier.padding(16.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    WeatherAppTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            NavDrawerMainScreen(
                state = WeatherState(),
                settings = Settings(),
                navController = rememberNavController(),
                pullState = rememberPullToRefreshState(),
                onPermissionRequest = {},
                onSettingsButtonPress = {},
                onRefresh = {},
                onActiveWeatherChanged = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Preview(
    showBackground = true, showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun MainScreenDarkPreview() {
    WeatherAppTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            NavDrawerMainScreen(
                state = WeatherState(),
                settings = Settings(),
                navController = rememberNavController(),
                pullState = rememberPullToRefreshState(),
                onPermissionRequest = {},
                onSettingsButtonPress = {},
                onRefresh = {},
                onActiveWeatherChanged = {}
            )
        }
    }
}