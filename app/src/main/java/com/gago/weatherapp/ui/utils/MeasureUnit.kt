package com.gago.weatherapp.ui.utils

import androidx.annotation.StringRes
import com.gago.weatherapp.R

enum class MeasureUnit(
    val unit: String,
    @StringRes val stringRes: Int,
    val windSpeedText: String,
    val tempUnitText: String
) {
    METRIC("metric", R.string.metric_unit, "m/s", "°C"),
    IMPERIAL("imperial", R.string.imperial_unit, "mph", "°F"),
    STANDARD("standard", R.string.standard_unit, "m/s", "K")
}
