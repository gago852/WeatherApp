package com.gago.weatherapp.ui.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Locale

class GetCurrentLanguageTest {

    @After
    fun tearDown() {
        // back to "follow the system" so the static state does not leak between tests
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    private fun contextWithLocale(locale: Locale): Context {
        val localeList = mock<LocaleList>()
        whenever(localeList.get(0)).thenReturn(locale)
        val configuration = mock<Configuration>()
        whenever(configuration.locales).thenReturn(localeList)
        val resources = mock<Resources>()
        whenever(resources.configuration).thenReturn(configuration)
        val context = mock<Context>()
        whenever(context.resources).thenReturn(resources)
        return context
    }

    @Test
    fun `in-app language wins over the context locale`() {
        // device/application context in English, per-app locale set to Spanish (the
        // API<33 case: AppCompat never localizes the application context)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))

        assertThat(getCurrentLanguage(contextWithLocale(Locale.ENGLISH)), `is`("es"))
    }

    @Test
    fun `without in-app language it falls back to the context locale`() {
        assertThat(getCurrentLanguage(contextWithLocale(Locale.FRENCH)), `is`("fr"))
    }

    @Test
    fun `unsupported languages map to english`() {
        assertThat(getCurrentLanguage(contextWithLocale(Locale.GERMAN)), `is`("en"))

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("de"))
        assertThat(getCurrentLanguage(contextWithLocale(Locale.ENGLISH)), `is`("en"))
    }
}
