package com.gago.weatherapp.domain.usecase

import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.fakes.FakeDataStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
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

    // --- removeCity ---

    @Test
    fun removeCity_inactiveCity_justDisappears() = runTest {
        val other = storedCity.copy(name = "Cartagena", lat = 10.39, isActive = false)
        val dataStore =
            FakeDataStore(Settings(listWeather = persistentListOf(storedCity, other)))
        val useCase = ManageCitiesUseCase(dataStore)

        val newActive = useCase.removeCity(other)

        assertThat(newActive, `is`(nullValue()))
        val listWeather = dataStore.data.first().listWeather
        assertThat(listWeather.size, `is`(1))
        assertThat(listWeather.first().isActive, `is`(true))
    }

    @Test
    fun removeCity_activeCity_promotesTheFirstRemainingOne() = runTest {
        val other = storedCity.copy(name = "Cartagena", lat = 10.39, isActive = false)
        val dataStore =
            FakeDataStore(Settings(listWeather = persistentListOf(storedCity, other)))
        val useCase = ManageCitiesUseCase(dataStore)

        val newActive = useCase.removeCity(storedCity)

        assertThat(newActive?.name, `is`("Cartagena"))
        val settings = dataStore.data.first()
        assertThat(settings.listWeather.size, `is`(1))
        assertThat(settings.listWeather.first().isActive, `is`(true))
        // forces the next refresh for the promoted city
        assertThat(settings.lastUpdate, `is`(0L))
    }

    @Test
    fun removeCity_lastCity_leavesTheListEmpty() = runTest {
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(storedCity)))
        val useCase = ManageCitiesUseCase(dataStore)

        val newActive = useCase.removeCity(storedCity)

        assertThat(newActive, `is`(nullValue()))
        assertThat(dataStore.data.first().listWeather.isEmpty(), `is`(true))
    }

    @Test
    fun removeCity_gpsCity_isANoOp() = runTest {
        val dataStore =
            FakeDataStore(Settings(listWeather = persistentListOf(storedCity, gpsCity)))
        val useCase = ManageCitiesUseCase(dataStore)

        val newActive = useCase.removeCity(gpsCity)

        assertThat(newActive, `is`(nullValue()))
        assertThat(dataStore.data.first().listWeather.size, `is`(2))
    }

    // --- restoreCity ---

    @Test
    fun restoreCity_reinsertsAtTheOriginalPositionAndReactivates() = runTest {
        val other = storedCity.copy(name = "Cartagena", lat = 10.39, isActive = true)
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(other)))
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.restoreCity(storedCity, index = 0)

        val listWeather = dataStore.data.first().listWeather
        assertThat(listWeather.size, `is`(2))
        assertThat(listWeather[0], `is`(storedCity))
        // the restored active city deactivates the others
        assertThat(listWeather.count { it.isActive }, `is`(1))
        assertThat(listWeather.first { it.isActive }.name, `is`("Barranquilla"))
    }

    @Test
    fun restoreCity_indexBeyondTheEndAppends() = runTest {
        val dataStore = FakeDataStore(Settings())
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.restoreCity(storedCity.copy(isActive = false), index = 7)

        assertThat(dataStore.data.first().listWeather.size, `is`(1))
    }

    // --- reorderCities ---

    @Test
    fun reorderCities_appliesTheNewOrder() = runTest {
        val other = storedCity.copy(name = "Cartagena", lat = 10.39, isActive = false)
        val dataStore =
            FakeDataStore(Settings(listWeather = persistentListOf(storedCity, other, gpsCity)))
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.reorderCities(listOf(gpsCity, storedCity, other))

        val listWeather = dataStore.data.first().listWeather
        assertThat(listWeather[0], `is`(gpsCity))
        assertThat(listWeather[1], `is`(storedCity))
        assertThat(listWeather[2], `is`(other))
    }

    @Test
    fun reorderCities_withMismatchedElements_isIgnored() = runTest {
        val dataStore =
            FakeDataStore(Settings(listWeather = persistentListOf(storedCity, gpsCity)))
        val useCase = ManageCitiesUseCase(dataStore)

        useCase.reorderCities(listOf(storedCity))

        assertThat(dataStore.data.first().listWeather.size, `is`(2))
        assertThat(dataStore.data.first().listWeather[0], `is`(storedCity))
    }

    // --- recordSearch ---

    @Test
    fun recordSearch_keepsMostRecentFirstDedupedAndCappedAtFive() = runTest {
        val dataStore = FakeDataStore(Settings())
        val useCase = ManageCitiesUseCase(dataStore)

        repeat(6) { index ->
            useCase.recordSearch("City$index", index.toDouble(), 0.0)
        }
        // repeating an existing name moves it to the front without duplicating
        useCase.recordSearch("City3", 3.0, 0.0)

        val history = dataStore.data.first().searchHistory
        assertThat(history.size, `is`(Settings.MAX_SEARCH_HISTORY))
        assertThat(history.first().name, `is`("City3"))
        assertThat(history.count { it.name == "City3" }, `is`(1))
        // the oldest entries fell off
        assertThat(history.none { it.name == "City0" }, `is`(true))
    }
}
