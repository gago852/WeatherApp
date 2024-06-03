package com.gago.weatherapp.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
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
import com.gago.weatherapp.ui.navigation.AppScreens
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.ReasonsForRefresh
import kotlinx.collections.immutable.persistentListOf
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

    val pullState = rememberPullToRefreshState()


    val settingValue =
        if (isSetup) Settings() else weatherViewModel.settings.collectAsState(initial = Settings()).value

    if (isSetup) {
        pullState.startRefresh()
    }

    val dialogQueue = weatherViewModel.visiblePermissionDialogQueue
//    Log.d("StateOnMainScreen", state.toString())
//    Log.d("StateOnMainScreenSettings", settingValue.toString())

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
                        pullState.startRefresh()
                    }
                }

            }

        }
    )

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

    LifecycleResumeEffect {
        Log.d("LifecycleResumeEffect", "resume ${weatherViewModel.wentToSettings}")
        if (weatherViewModel.wentToSettings) {
            weatherViewModel.setReasonForRefresh(ReasonsForRefresh.PULL)
            pullState.startRefresh()
            weatherViewModel.setWentToSettings(false)
        }
        onPauseOrDispose {
            Log.d("LifecycleResumeEffect", "pause $weatherViewModel.wentToSettings")
        }
    }


    if (pullState.isRefreshing) {
        when (weatherViewModel.reasonForRefresh) {

            ReasonsForRefresh.WEATHER_CHANGED -> run {
                val settingChanged = weatherViewModel.settingChanged?.copy()
                settingChanged?.let {

                    weatherViewModel.loadAnotherWeather(it)
                    weatherViewModel.setSettingChanged(null)
                } ?: run {
                    pullState.endRefresh()
                }
                weatherViewModel.setReasonForRefresh(ReasonsForRefresh.PULL)
            }

            ReasonsForRefresh.STARTUP -> run {

                LaunchedEffect(Unit) {
                    val setting = weatherViewModel.getInitialSetUp()
                    Log.d("startup", "lanzo")
                    setting?.let {
                        val listWeatherStoredActive = it.listWeather.filter { lit -> lit.isActive }

                        val weatherCurrent = listWeatherStoredActive.firstOrNull()

                        weatherCurrent?.let { weatherLocal ->
                            if (weatherLocal.isGps) {
                                if (!it.permissionAccepted) {
                                    locationPermissionResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                } else {
                                    weatherViewModel.loadLocationWeather()
                                }
                            } else {
                                weatherViewModel.loadWeatherFromCurrent(weatherLocal)
                            }
                        }
                        pullState.endRefresh()
                    }
                    weatherViewModel.initialStartUp()
                }

            }

            else -> weatherViewModel.refreshWeather()
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
    ) {
        weatherViewModel.setSettingChanged(it)
        weatherViewModel.setReasonForRefresh(ReasonsForRefresh.WEATHER_CHANGED)
        pullState.startRefresh()
    }

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
    onActiveWeatherChanged: (Settings) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 28.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(modifier = Modifier.padding(top = 18.dp),
                        onClick = {
                            onSettingsButtonPress()
                            navController.navigate(AppScreens.SettingScreen.route)
                            scope.launch {
                                drawerState.close()
                            }
                        }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
                settings.listWeather.forEach { weather ->
                    NavigationDrawerItem(modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                        label = {
                            Text(text = weather.name)
                        },
                        icon = {
                            if (weather.isGps) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "From GPS"
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

        Scaffold(modifier =
        if (state.error != null || state.weatherCurrent != null || settings.permissionAccepted)
            Modifier.nestedScroll(pullState.nestedScrollConnection)
        else
            Modifier,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                val activeWeather = settings.listWeather.find { it.isActive }?.let {
                    "- ${it.name}"
                } ?: ""

                TopAppBar(

                    title = { Text(text = "Weather App $activeWeather") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            },
            content = { paddingValues ->

                var noWeather by remember {
                    mutableStateOf(true)
                }

                Box(modifier = Modifier.padding(paddingValues)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = if (noWeather) Alignment.CenterHorizontally else Alignment.Start,
                        verticalArrangement = if (noWeather) Arrangement.Center else Arrangement.Top
                    ) {
                        if (state.isLoading) {
                            noWeather = true
                        } else if (pullState.isRefreshing) {
                            pullState.endRefresh()
                        }

                        state.error?.let {
                            noWeather = true
                            val error = stringResource(id = it)
                            if (state.weatherCurrent != null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            } else {
                                Text(text = error)
                            }

                        }

                        state.weatherCurrent?.let {
                            noWeather = false
                            Text(text = it.toString())
                        }

                        if (!state.isLoading && state.error == null && state.weatherCurrent == null) {
                            noWeather = true
                            if (!settings.permissionAccepted) {
                                NoWeatherScreen() {
                                    onPermissionRequest()
                                }
                            } else {
                                Text(text = "swipe to load")
                            }
                        }
                    }

                    PullToRefreshContainer(
                        state = pullState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )

                }

            })
    }
}

@Composable
fun NoWeatherScreen(onPermissionRequest: () -> Unit) {


    Card(modifier = Modifier.padding(32.dp)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                text = stringResource(R.string.welcome_title)
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                text = stringResource(R.string.welcome_message)
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Button(onClick = { onPermissionRequest() }) {
                Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "Gps Activate")
                Text(text = "Look with GPS")
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
                rememberPullToRefreshState(),
                onPermissionRequest = {

                },
                onSettingsButtonPress = {}
            ) {}
        }
    }
}

/*
weatherCurrent = Weather(
                        1, "1", 1,
                        Sys("1", 2, 1, 1, 1), "",
                        WeatherCondition("", WeatherTypeIcon.Mist, 2, ""),
                        WeatherData(2.2, 5, 5, 51.5, 1.5, 5.5),
                        Wind(5), 5, null, null
                    )

 */