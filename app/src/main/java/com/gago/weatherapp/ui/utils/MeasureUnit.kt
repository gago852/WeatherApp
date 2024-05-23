package com.gago.weatherapp.ui.utils

import androidx.annotation.StringRes
import com.gago.weatherapp.R

enum class MeasureUnit(val unit: String, @StringRes val stringRes: Int) {
    METRIC("metric", R.string.metric_unit),
    IMPERIAL("imperial", R.string.imperial_unit)
}