package com.gago.weatherapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gago.weatherapp.ui.main.MainScreen
import com.gago.weatherapp.ui.settings.SettingsScreen

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.MainScreen.route) {
        composable(route = AppScreens.MainScreen.route) {
            MainScreen(navController = navController)
        }
        composable(route = AppScreens.SettingScreen.route) {
            SettingsScreen(navController = navController)
        }
    }
}