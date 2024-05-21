package com.gago.weatherapp.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.gago.weatherapp.ui.WeatherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()



    ModalNavigationDrawer(drawerState = drawerState,
        drawerContent = {


            ModalDrawerSheet {
                var selectedItem by remember {
                    mutableStateOf("Favoritos")
                }
                val menuItems = mapOf<String, ImageVector>(
                    Pair("Favoritos", Icons.Default.Favorite),
                    Pair("modo ahorro", Icons.Default.Refresh)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 28.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(modifier = Modifier.padding(top = 18.dp),
                        onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
                menuItems.forEach {
                    NavigationDrawerItem(modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                        label = {
                            Icon(
                                imageVector = it.value,
                                contentDescription = it.key
                            )
                        }, selected = selectedItem == it.key, onClick = {
                            scope.launch {
                                drawerState.close()
                            }
                            selectedItem = it.key
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
            content = {
                it
            })
    }


}