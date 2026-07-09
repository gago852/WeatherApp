package com.gago.weatherapp.ui.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class TimeFormattersTest {

    private lateinit var defaultTimeZone: TimeZone

    @Before
    fun setUp() {
        defaultTimeZone = TimeZone.getDefault()
        // formatFullDateTime uses the device time zone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(defaultTimeZone)
    }

    @Test
    fun `formatFullDateTime formats the epoch in the device time zone`() {
        assertThat(formatFullDateTime(0L, Locale.US), `is`("1970-01-01 00:00:00"))
    }

    @Test
    fun `formatDayOfWeek formats at the remote offset with the given locale`() {
        // 1970-01-05 00:00 UTC was a Monday
        val mondayUtc = 4 * 86_400L

        assertThat(formatDayOfWeek(mondayUtc, 0L, Locale.US), `is`("Monday"))
        assertThat(formatDayOfWeek(mondayUtc, 0L, Locale.forLanguageTag("es")), `is`("lunes"))
        // UTC-4: midnight UTC is still Sunday locally
        assertThat(formatDayOfWeek(mondayUtc, -14_400L, Locale.US), `is`("Sunday"))
    }

    @Test
    fun `formatTwelveHourTime formats at the remote offset`() {
        assertThat(formatTwelveHourTime(0L, 0L, Locale.US), `is`("12:00 AM"))
        assertThat(formatTwelveHourTime(43_200L, 0L, Locale.US), `is`("12:00 PM"))
        // UTC-4: midnight UTC is 8:00 PM the previous day
        assertThat(formatTwelveHourTime(0L, -14_400L, Locale.US), `is`("08:00 PM"))
    }
}
