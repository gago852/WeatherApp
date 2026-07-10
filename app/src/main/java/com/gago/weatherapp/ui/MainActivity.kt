package com.gago.weatherapp.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.ui.navigation.AppNavigator
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

// AppCompatActivity (instead of ComponentActivity) so per-app locales work below API 33
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala el SplashScreen lo antes posible
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // VM de arranque para controlar la condición del Splash
        val startupViewModel: AppStartupViewModel by viewModels()

        // Mantener el splash hasta que isReady sea true
        splash.setKeepOnScreenCondition {
            !startupViewModel.isReady.value
        }

        setContent {
            val settings by startupViewModel.settings.collectAsState()
            val current = settings ?: Settings()
            val darkTheme = when (current.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            WeatherAppTheme(
                darkTheme = darkTheme,
                dynamicColor = current.dynamicColor
            ) {
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
