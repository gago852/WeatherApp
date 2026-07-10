package com.gago.weatherapp.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Date/time formatting lives in the UI layer so text is produced with the locale active at
 * render time: domain models carry raw epochs and a configuration (language) change simply
 * recomposes with the new locale, without refetching.
 */

/** Locale currently applied to the UI; reading it makes recomposition follow config changes. */
@Composable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

/** "yyyy-MM-dd HH:mm:ss" in the device time zone (used for "last updated"). */
fun formatFullDateTime(epochSeconds: Long, locale: Locale): String {
    val dateTime = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(epochSeconds),
        ZoneId.systemDefault()
    )
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", locale).format(dateTime)
}

/** Full day-of-week name ("EEEE") at the remote UTC offset, in seconds. */
fun formatDayOfWeek(epochSeconds: Long, timeZoneOffset: Long, locale: Locale): String {
    val dateTime = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(epochSeconds),
        ZoneOffset.ofTotalSeconds(timeZoneOffset.toInt())
    )
    return DateTimeFormatter.ofPattern("EEEE", locale).format(dateTime)
}

/** Compact hour ("2 PM") at the remote UTC offset, for the hourly forecast row. */
fun formatShortHour(epochSeconds: Long, timeZoneOffset: Long, locale: Locale): String {
    val dateTime = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(epochSeconds),
        ZoneOffset.ofTotalSeconds(timeZoneOffset.toInt())
    )
    return DateTimeFormatter.ofPattern("h a", locale).format(dateTime)
}

/** Twelve-hour time ("hh:mm a") at the remote UTC offset, in seconds. */
fun formatTwelveHourTime(epochSeconds: Long, timeZoneOffset: Long, locale: Locale): String {
    val dateTime = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(epochSeconds),
        ZoneOffset.ofTotalSeconds(timeZoneOffset.toInt())
    )
    return DateTimeFormatter.ofPattern("hh:mm a", locale).format(dateTime)
}
