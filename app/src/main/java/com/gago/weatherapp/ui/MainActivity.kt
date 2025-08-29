package com.gago.weatherapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gago.weatherapp.ui.navigation.AppNavigator
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala el SplashScreen lo antes posible
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // VM de arranque para controlar la condición del Splash
        val startupViewModel: AppStartupViewModel by viewModels()

        // Mantener el splash hasta que isReady sea true
        splash.setKeepOnScreenCondition {
            !startupViewModel.isReady.value
        }

        setContent {
            WeatherAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }
}