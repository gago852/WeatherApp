package com.gago.weatherapp.ui.navigation

sealed class AppScreens(val route: String) {
    object MainScreen : AppScreens("main_screen")
    object SettingScreen : AppScreens("setting_screen")
}