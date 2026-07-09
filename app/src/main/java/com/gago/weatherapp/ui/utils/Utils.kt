package com.gago.weatherapp.ui.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.gago.weatherapp.R
import com.gago.weatherapp.domain.utils.DataError

const val ONE_MINUTE_IN_MILLIS = 60000
fun getCurrentLanguage(context: Context): String {

    // The in-app language must be read from AppCompatDelegate: below API 33 it is only
    // applied to activity contexts, so an application context still has the device locale.
    val language = AppCompatDelegate.getApplicationLocales()[0]?.language
        ?: context.resources.configuration.locales.get(0).language

    return when (language) {
        "en" -> "en"
        "es" -> "es"
        "fr" -> "fr"
        else -> "en"
    }
}

/** OWM AQI levels: 1 good … 5 very poor. */
fun getAqiText(aqi: Int): Int {
    return when (aqi) {
        1 -> R.string.aqi_good
        2 -> R.string.aqi_fair
        3 -> R.string.aqi_moderate
        4 -> R.string.aqi_poor
        else -> R.string.aqi_very_poor
    }
}

fun getErrorText(error: DataError): Int {
    return when (error) {
        DataError.Network.REQUEST_TIMEOUT -> R.string.error_request_timeout
        DataError.Network.TOO_MANY_REQUESTS -> R.string.error_too_many_requests
        DataError.Network.NO_INTERNET -> R.string.error_no_internet
        DataError.Network.PAYLOAD_TOO_LARGE -> R.string.error_payload_too_large
        DataError.Network.SERVER_ERROR -> R.string.error_server_error
        DataError.Network.SERIALIZATION -> R.string.error_bad_request
        DataError.Network.UNAUTHORIZED -> R.string.error_unauthorized
        DataError.Network.FORBIDDEN -> R.string.error_forbidden
        DataError.Network.NOT_FOUND -> R.string.error_not_found
        DataError.Network.UNKNOWN -> R.string.error_generic
        else -> R.string.error_generic
    }
}

fun getPlacesErrorMessage(error: DataError.Places): Int {
    return when (error) {
        DataError.Places.QUOTA_EXCEEDED -> R.string.error_places_quota_exceeded
        DataError.Places.OVER_QUERY_LIMIT -> R.string.error_places_over_query_limit
        DataError.Places.INVALID_REQUEST -> R.string.error_places_invalid_request
        DataError.Places.NOT_FOUND -> R.string.error_places_not_found
        DataError.Places.NO_INTERNET -> R.string.error_no_internet
        DataError.Places.UNKNOWN -> R.string.error_generic
    }
}

fun String.capitalizeWords(delimiter: String = " ") =
    split(delimiter).joinToString(delimiter) { word ->

        val smallCaseWord = word.lowercase()
        smallCaseWord.replaceFirstChar(Char::titlecaseChar)

    }

