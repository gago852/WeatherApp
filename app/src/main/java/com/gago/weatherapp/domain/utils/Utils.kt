package com.gago.weatherapp.domain.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertDateFromUnix(unixTime: Long): String {
    val date = Date(unixTime * 1000)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(date)
}