package com.gago.weatherapp.domain.usecase

import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.fakes.FakeDataStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

class ManageCitiesUseCaseTest {

    private val storedCity = WeatherLocal(
        name = "Barranquilla",
        lat = 10.96,
        lon = -74.79,
        isActive = true,
        isGps = false
    )
    private val gpsCity = WeatherLocal(
        name = "Bogota",
        lat = 4.71,
        lon = -74.07,
        isActive = false,
        isGps = true
    )

    @Test
    fun deactivateAllCities_everyCityEndsInactive() = runTest {
        val dataStore =
            FakeDataStore(Settings(listWeather = persistentListOf(storedCity, gpsCity)))
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.deactivateAllCities()

        val listWeather = dataStore.data.first().listWeather
        assertThat(listWeather.none { it.isActive }, `is`(true))
        assertThat(listWeather.size, `is`(2))
    }

    @Test
    fun upsertGpsCity_updatesExistingGpsEntryInPlace() = runTest {
        val dataStore =
            FakeDataStore(Settings(listWeather = persistentListOf(gpsCity, storedCity)))
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.upsertGpsCity(latitude = 6.24, longitude = -75.58, name = "Medellin")

        val listWeather = dataStore.data.first().listWeather
        assertThat(listWeather.size, `is`(2))
        val updated = listWeather[0]
        assertThat(updated.name, `is`("Medellin"))
        assertThat(updated.lat, `is`(6.24))
        assertThat(updated.lon, `is`(-75.58))
        assertThat(updated.isActive, `is`(true))
        assertThat(updated.isGps, `is`(true))
    }

    @Test
    fun upsertGpsCity_addsGpsEntryWhenNoneExists() = runTest {
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(storedCity)))
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.upsertGpsCity(latitude = 6.24, longitude = -75.58, name = "Medellin")

        val listWeather = dataStore.data.first().listWeather
        assertThat(listWeather.size, `is`(2))
        val added = listWeather.last()
        assertThat(added.name, `is`("Medellin"))
        assertThat(added.isGps, `is`(true))
        assertThat(added.isActive, `is`(true))
    }

    @Test
    fun addOrActivateCity_replacesEntryWithSameCoordinatesAndDeactivatesOthers() = runTest {
        val dataStore = FakeDataStore(
            Settings(
                lastUpdate = 123L,
                listWeather = persistentListOf(storedCity, gpsCity)
            )
        )
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.addOrActivateCity(latitude = storedCity.lat, longitude = storedCity.lon, name = "Killa")

        val settings = dataStore.data.first()
        assertThat(settings.lastUpdate, `is`(0L))
        assertThat(settings.listWeather.size, `is`(2))
        assertThat(settings.listWeather[0].name, `is`("Killa"))
        assertThat(settings.listWeather[0].isActive, `is`(true))
        assertThat(settings.listWeather[0].isGps, `is`(false))
        assertThat(settings.listWeather[1].isActive, `is`(false))
    }

    @Test
    fun addOrActivateCity_appendsNewCityAndDeactivatesOthers() = runTest {
        val dataStore = FakeDataStore(
            Settings(
                lastUpdate = 123L,
                listWeather = persistentListOf(storedCity)
            )
        )
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.addOrActivateCity(latitude = 6.24, longitude = -75.58, name = "Medellin")

        val settings = dataStore.data.first()
        assertThat(settings.lastUpdate, `is`(0L))
        assertThat(settings.listWeather.size, `is`(2))
        assertThat(settings.listWeather[0].isActive, `is`(false))
        assertThat(settings.listWeather[1].name, `is`("Medellin"))
        assertThat(settings.listWeather[1].isActive, `is`(true))
    }

    @Test
    fun applySettings_replacesTheWholeSnapshot() = runTest {
        val dataStore = FakeDataStore(Settings(lastUpdate = 123L))
        val useCase = ManageCitiesUseCase(dataStore)
        val newSettings = Settings(lastUpdate = 456L, listWeather = persistentListOf(storedCity))

        useCase.applySettings(newSettings)

        assertThat(dataStore.data.first(), `is`(newSettings))
    }
}
