package com.gago.weatherapp.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

private val permissionsToRequest = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun MainScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {

    val state = weatherViewModel.state
    val settingValue = weatherViewModel.settings.collectAsState(initial = Settings()).value

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
                weatherViewModel.loadLocationWeather()
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

    val listWeatherStoredActive = settingValue.listWeather.filter { it.isActive }

    val weatherCurrent = listWeatherStoredActive.firstOrNull()

    weatherCurrent?.let {
        if (it.isGps) {
            weatherViewModel.loadLocationWeather()
        } else {
            weatherViewModel.loadWeatherFromCurrent(it)
        }
    }

    NavDrawerMainScreen(
        state = state,
        settings = settingValue,
        navController = navController,
        onPermissionRequest = {
            locationPermissionResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    ) {
        weatherViewModel.loadAnotherWeather(it)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerMainScreen(
    state: WeatherState,
    settings: Settings,
    navController: NavController,
    onPermissionRequest: () -> Unit,
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
                            if (weather.isGps) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "From GPS"
                                )
                            }
                        }, selected = weather.isActive, onClick = {
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
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                TopAppBar(

                    title = { Text(text = "Weather App") },
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = if (noWeather) Alignment.CenterHorizontally else Alignment.Start,
                    verticalArrangement = if (noWeather) Arrangement.Center else Arrangement.Top
                ) {
                    if (state.isLoading) {
                        noWeather = true
                        CircularProgressIndicator()
                    }

                    state.error?.let {
                        noWeather = true
                        val error = stringResource(id = it)
                        scope.launch {
                            snackbarHostState.showSnackbar(error)
                        }
                        Text(text = "texto de ejemplo")
                    }

                    state.weatherCurrent?.let {
                        noWeather = false
                        Text(text = "texto de ejemplo")
                    }

                    if (!state.isLoading && state.error == null && state.weatherCurrent == null) {
                        noWeather = true
                        NoWeatherScreen() {
                            onPermissionRequest()
                        }
                    }
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
            Button(onClick = { }) {
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
                onPermissionRequest = {

                }
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