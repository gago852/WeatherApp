package com.gago.weatherapp.domain.usecase

import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.domain.usecase.RefreshWeatherUseCase.RefreshDecision
import kotlinx.collections.immutable.persistentListOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.junit.Test

class RefreshWeatherUseCaseTest {

    private val useCase = RefreshWeatherUseCase()
    private val now = 1_000_000_000L

    private fun city(isActive: Boolean = true, isGps: Boolean = false) = WeatherLocal(
        name = "Barranquilla",
        lat = 10.96,
        lon = -74.79,
        isActive = isActive,
        isGps = isGps
    )

    @Test
    fun nullSettings_returnsNoSettings() {
        val decision = useCase(null, hasWeatherLoaded = true, now = now)

        assertThat(decision, `is`(instanceOf(RefreshDecision.NoSettings::class.java)))
    }

    @Test
    fun freshDataAndWeatherLoaded_returnsNotNeeded() {
        val settings = Settings(lastUpdate = now - 30_000L, listWeather = persistentListOf(city()))

        val decision = useCase(settings, hasWeatherLoaded = true, now = now)

        assertThat(decision, `is`(instanceOf(RefreshDecision.NotNeeded::class.java)))
    }

    @Test
    fun freshDataButLanguageChanged_refreshesAnyway() {
        val settings = Settings(lastUpdate = now - 30_000L, listWeather = persistentListOf(city()))

        val decision = useCase(settings, hasWeatherLoaded = true, languageChanged = true, now = now)

        assertThat(decision, `is`(instanceOf(RefreshDecision.FromCoordinates::class.java)))
    }

    @Test
    fun freshDataButNoWeatherOnScreen_refreshesAnyway() {
        val settings = Settings(lastUpdate = now - 30_000L, listWeather = persistentListOf(city()))

        val decision = useCase(settings, hasWeatherLoaded = false, now = now)

        assertThat(decision, `is`(instanceOf(RefreshDecision.FromCoordinates::class.java)))
    }

    @Test
    fun staleDataWithActiveGpsCity_returnsFromGps() {
        val settings = Settings(
            lastUpdate = now - 120_000L,
            listWeather = persistentListOf(city(isGps = true))
        )

        val decision = useCase(settings, hasWeatherLoaded = true, now = now)

        assertThat(decision, `is`(instanceOf(RefreshDecision.FromGps::class.java)))
    }

    @Test
    fun staleDataWithActiveStoredCity_returnsFromCoordinatesWithThatCity() {
        val activeCity = city()
        val settings = Settings(
            lastUpdate = now - 120_000L,
            listWeather = persistentListOf(city(isActive = false, isGps = true), activeCity)
        )

        val decision = useCase(settings, hasWeatherLoaded = true, now = now)

        assertThat(decision, `is`(instanceOf(RefreshDecision.FromCoordinates::class.java)))
        decision as RefreshDecision.FromCoordinates
        assertThat(decision.city, `is`(activeCity))
        assertThat(decision.settings, `is`(settings))
    }

    @Test
    fun noActiveCityWithPermission_returnsFromGps() {
        val settings = Settings(
            lastUpdate = 0L,
            permissionAccepted = true,
            listWeather = persistentListOf(city(isActive = false))
        )

        val decision = useCase(settings, hasWeatherLoaded = false, now = now)

        assertThat(decision, `is`(instanceOf(RefreshDecision.FromGps::class.java)))
    }

    @Test
    fun noActiveCityWithoutPermission_returnsNoCityWithoutPermission() {
        val settings = Settings(lastUpdate = 0L, permissionAccepted = false)

        val decision = useCase(settings, hasWeatherLoaded = false, now = now)

        assertThat(
            decision,
            `is`(instanceOf(RefreshDecision.NoCityWithoutPermission::class.java))
        )
    }
}
