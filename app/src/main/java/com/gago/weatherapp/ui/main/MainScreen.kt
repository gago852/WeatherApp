package com.gago.weatherapp.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.ui.WeatherState
import com.gago.weatherapp.ui.WeatherViewModel
import com.gago.weatherapp.ui.navigation.AppScreens
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {

    val state = weatherViewModel.state
    val settingValue = weatherViewModel.settings.collectAsState(initial = Settings()).value
    NavDrawerMainScreen(state = state, settings = settingValue, navController = navController) {
        weatherViewModel.loadAnotherWeather(it)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavDrawerMainScreen(
    state: WeatherState,
    settings: Settings,
    navController: NavController,
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
                        onClick = { navController.navigate(AppScreens.SettingScreen.route) }) {
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
                            }
                        })
                }

            }


        }) {
        Scaffold(
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "aaaa")
                }
            })
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    WeatherAppTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            NavDrawerMainScreen(
                state = WeatherState(),
                settings = Settings(),
                navController = rememberNavController()
            ) {}
        }
    }
}