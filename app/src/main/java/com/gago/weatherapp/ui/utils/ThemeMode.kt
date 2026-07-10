package com.gago.weatherapp.ui.utils

import androidx.annotation.StringRes
import com.gago.weatherapp.R

enum class ThemeMode(@StringRes val stringRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}
