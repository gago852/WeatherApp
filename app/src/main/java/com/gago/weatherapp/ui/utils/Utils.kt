package com.gago.weatherapp.ui.utils

import android.content.Context
import com.gago.weatherapp.R
import com.gago.weatherapp.domain.utils.DataError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val ONE_MINUTE_IN_MILLIS = 60000
fun getCurrentLanguage(context: Context): String {

    val language = context.resources.configuration.locales.get(0).language

    return when (language) {
        "en" -> "en"
        "es" -> "es"
        else -> "en"
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

fun String.capitalizeWords(delimiter: String = " ") =
    split(delimiter).joinToString(delimiter) { word ->

        val smallCaseWord = word.lowercase()
        smallCaseWord.replaceFirstChar(Char::titlecaseChar)

    }

