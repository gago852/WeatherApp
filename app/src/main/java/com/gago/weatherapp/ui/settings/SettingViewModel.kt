package com.gago.weatherapp.ui.settings

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.repository.PlacesClientProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val dataStore: DataStore<Settings>,
    private val placesClientProvider: PlacesClientProvider
) : ViewModel() {

    val settings = dataStore.data.catch {
        emit(Settings())
    }

    fun saveChangeSettings(settings: Settings) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(
                    unitOfMeasurement = settings.unitOfMeasurement,
                    listWeather = settings.listWeather,
                    themeMode = settings.themeMode,
                    dynamicColor = settings.dynamicColor,
                    refreshIntervalMinutes = settings.refreshIntervalMinutes,
                    lastUpdate = 0L
                )
            }
        }
    }

    /**
     * Applies the in-app language: persists it, hands it to the platform (which recreates the
     * activity) and re-initializes the Places SDK, whose locale is fixed at initialize() time.
     * An empty [languageTag] returns to the system language.
     */
    fun changeLanguage(languageTag: String) {
        viewModelScope.launch {
            dataStore.updateData { it.copy(language = languageTag, lastUpdate = 0L) }
            val locale = if (languageTag.isEmpty()) {
                // App resources may still carry the previous per-app locale here
                Resources.getSystem().configuration.locales[0]
            } else {
                Locale.forLanguageTag(languageTag)
            }
            placesClientProvider.reinitialize(locale)
            AppCompatDelegate.setApplicationLocales(
                if (languageTag.isEmpty()) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(languageTag)
                }
            )
        }
    }
}
