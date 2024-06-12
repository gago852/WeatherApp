package com.gago.weatherapp.domain.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun convertDateFromUnixLocalTimeZone(unixTime: Long): String {

    val zonedDateTimeLocalTZ = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(unixTime),
        ZoneId.systemDefault()
    )

    val formatterPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


    return formatterPattern.format(zonedDateTimeLocalTZ)
}

fun convertDateFromUnixLocatedTimeZone(unixTime: Long, timeZoneOffset: Long): String {

    val zonedDateTimeRemoteTZ = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(unixTime),
        ZoneOffset.ofTotalSeconds(timeZoneOffset.toInt())
    )
    val formatterPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    return formatterPattern.format(zonedDateTimeRemoteTZ)
}

fun convertDateWithoutTimeFromUnixLocatedTimeZone(unixTime: Long, timeZoneOffset: Long): String {

    val zonedDateTimeRemoteTZ = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(unixTime),
        ZoneOffset.ofTotalSeconds(timeZoneOffset.toInt())
    )
    val formatterPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    return formatterPattern.format(zonedDateTimeRemoteTZ)
}

fun getHourFromUnixLocatedTimeZone(unixTime: Long, timeZoneOffset: Long): String {

    val zonedDateTimeRemoteTZ = ZonedDateTime.ofInstant(
        Instant.ofEpochSecond(unixTime),
        ZoneOffset.ofTotalSeconds(timeZoneOffset.toInt())
    )
    val formatterPattern = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    return formatterPattern.format(zonedDateTimeRemoteTZ)
}