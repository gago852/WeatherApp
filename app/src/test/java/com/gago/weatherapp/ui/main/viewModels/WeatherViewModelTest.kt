package com.gago.weatherapp.ui.main.viewModels

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.LocaleList
import androidx.lifecycle.SavedStateHandle
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.domain.location.LocationTracker
import com.gago.weatherapp.domain.usecase.GetWeatherUseCase
import com.gago.weatherapp.domain.usecase.ManageCitiesUseCase
import com.gago.weatherapp.domain.usecase.RefreshWeatherUseCase
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.data.datastore.CachedWeather
import com.gago.weatherapp.data.datastore.WeatherCache
import com.gago.weatherapp.data.datastore.weatherCacheKey
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.fakes.FakeDataStore
import com.gago.weatherapp.fakes.FakeLocationTracker
import com.gago.weatherapp.fakes.FakeWeatherCacheDataStore
import com.gago.weatherapp.fakes.FakeWeatherRepository
import com.gago.weatherapp.ui.utils.MockData
import com.gago.weatherapp.rules.MainDispatcherRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val madrid = WeatherLocal(
        name = "Madrid",
        lat = 40.41,
        lon = -3.7,
        isActive = true,
        isGps = false
    )

    private fun mockApplication(): Application {
        val localeList = mock<LocaleList>()
        whenever(localeList.get(0)).thenReturn(Locale.ENGLISH)
        val configuration = mock<Configuration>()
        whenever(configuration.locales).thenReturn(localeList)
        val resources = mock<Resources>()
        whenever(resources.configuration).thenReturn(configuration)
        val application = mock<Application>()
        whenever(application.resources).thenReturn(resources)
        return application
    }

    private fun mockLocation(latitude: Double, longitude: Double): Location {
        val location = mock<Location>()
        whenever(location.latitude).thenReturn(latitude)
        whenever(location.longitude).thenReturn(longitude)
        return location
    }

    private fun buildViewModel(
        repository: FakeWeatherRepository = FakeWeatherRepository(),
        locationTracker: LocationTracker = FakeLocationTracker(),
        dataStore: FakeDataStore = FakeDataStore(),
        weatherCache: FakeWeatherCacheDataStore = FakeWeatherCacheDataStore()
    ) = WeatherViewModel(
        getWeatherUseCase = GetWeatherUseCase(repository, dataStore, weatherCache),
        refreshWeatherUseCase = RefreshWeatherUseCase(),
        manageCitiesUseCase = ManageCitiesUseCase(dataStore),
        locationTracker = locationTracker,
        dataStore = dataStore,
        savedStateHandle = SavedStateHandle(),
        context = mockApplication()
    )

    // --- load flow ---

    @Test
    fun `refreshWeather loads weather for the active stored city`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        viewModel.refreshWeather()
        advanceUntilIdle()

        assertThat(viewModel.state.isLoading, `is`(false))
        assertThat(viewModel.state.error, nullValue())
        assertThat(viewModel.state.weather, notNullValue())
        assertThat(repository.getWeatherCallCount, `is`(1))
        assertThat(repository.lastLatitude, `is`(madrid.lat))
        assertThat(repository.lastLongitude, `is`(madrid.lon))
        // a successful load stamps lastUpdate for the throttle window
        assertThat(dataStore.data.first().lastUpdate > 0L, `is`(true))
    }

    @Test
    fun `refreshWeather without stored city and permission denied sets refresh error`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(Settings(permissionAccepted = false))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        viewModel.refreshWeather()
        advanceUntilIdle()

        assertThat(viewModel.state.isLoading, `is`(false))
        assertThat(viewModel.state.error, `is`(R.string.refresh_error))
        assertThat(repository.getWeatherCallCount, `is`(0))
    }

    @Test
    fun `refreshWeather without stored city and permission accepted loads from gps`() = runTest {
        val repository = FakeWeatherRepository()
        val tracker = FakeLocationTracker(mockLocation(45.5, -73.55))
        val dataStore = FakeDataStore(Settings(permissionAccepted = true))
        val viewModel = buildViewModel(
            repository = repository,
            locationTracker = tracker,
            dataStore = dataStore
        )

        viewModel.refreshWeather()
        advanceUntilIdle()

        assertThat(repository.getWeatherCallCount, `is`(1))
        assertThat(repository.lastLatitude, `is`(45.5))
        val stored = dataStore.data.first().listWeather
        assertThat(stored.size, `is`(1))
        assertThat(stored.first().isGps, `is`(true))
        assertThat(stored.first().isActive, `is`(true))
        // the gps entry takes the city name returned by the API
        assertThat(stored.first().name, `is`("Montreal"))
    }

    @Test
    fun `refreshWeather sets location error when gps is unavailable`() = runTest {
        val tracker = FakeLocationTracker(location = null)
        val dataStore = FakeDataStore(Settings(permissionAccepted = true))
        val viewModel = buildViewModel(locationTracker = tracker, dataStore = dataStore)

        viewModel.refreshWeather()
        advanceUntilIdle()

        assertThat(viewModel.state.isLoading, `is`(false))
        assertThat(viewModel.state.error, `is`(R.string.error_location))
        assertThat(tracker.callCount, `is`(1))
    }

    // --- throttle ---

    @Test
    fun `refreshWeather skips the api inside the one minute throttle window`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        viewModel.refreshWeather()
        advanceUntilIdle()
        assertThat(repository.getWeatherCallCount, `is`(1))

        // lastUpdate was just stamped, weather is loaded: second refresh must not hit the api
        viewModel.refreshWeather()
        advanceUntilIdle()

        assertThat(repository.getWeatherCallCount, `is`(1))
        assertThat(viewModel.state.isLoading, `is`(false))
        assertThat(viewModel.state.error, nullValue())
    }

    @Test
    fun `refreshWeather refreshes again when lastUpdate is stale`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        viewModel.refreshWeather()
        advanceUntilIdle()
        assertThat(repository.getWeatherCallCount, `is`(1))

        // push lastUpdate outside the throttle window
        dataStore.updateData { it.copy(lastUpdate = it.lastUpdate - 61_000L) }
        viewModel.refreshWeather()
        advanceUntilIdle()

        assertThat(repository.getWeatherCallCount, `is`(2))
    }

    // --- auto refresh interval ---

    @Test
    fun `autoRefreshOnResume refreshes when data is older than the configured interval`() =
        runTest {
            val repository = FakeWeatherRepository()
            val dataStore = FakeDataStore(
                Settings(listWeather = persistentListOf(madrid), refreshIntervalMinutes = 15)
            )
            val viewModel = buildViewModel(repository = repository, dataStore = dataStore)
            viewModel.loadWeatherFromCurrent(madrid)
            advanceUntilIdle()
            assertThat(repository.getWeatherCallCount, `is`(1))

            // data on screen is now older than the 15 minute interval
            dataStore.updateData {
                it.copy(lastUpdate = System.currentTimeMillis() - 16 * 60_000L)
            }
            viewModel.autoRefreshOnResume()
            advanceUntilIdle()

            assertThat(repository.getWeatherCallCount, `is`(2))
        }

    @Test
    fun `autoRefreshOnResume does nothing while data is fresher than the interval`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(
            Settings(listWeather = persistentListOf(madrid), refreshIntervalMinutes = 15)
        )
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)
        viewModel.loadWeatherFromCurrent(madrid)
        advanceUntilIdle()

        viewModel.autoRefreshOnResume()
        advanceUntilIdle()

        assertThat(repository.getWeatherCallCount, `is`(1))
    }

    @Test
    fun `autoRefreshOnResume does nothing when the interval is manual`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(
            Settings(listWeather = persistentListOf(madrid), refreshIntervalMinutes = 0)
        )
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)
        viewModel.loadWeatherFromCurrent(madrid)
        advanceUntilIdle()

        dataStore.updateData { it.copy(lastUpdate = 0L) }
        viewModel.autoRefreshOnResume()
        advanceUntilIdle()

        assertThat(repository.getWeatherCallCount, `is`(1))
    }

    // --- active city change ---

    @Test
    fun `loadAnotherWeather persists the given settings and loads its active city`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        val paris = WeatherLocal(name = "Paris", lat = 48.85, lon = 2.35, isActive = true)
        val newSettings = Settings(
            listWeather = persistentListOf(madrid.copy(isActive = false), paris)
        )

        viewModel.loadAnotherWeather(newSettings)
        advanceUntilIdle()

        assertThat(repository.lastLatitude, `is`(paris.lat))
        assertThat(repository.lastLongitude, `is`(paris.lon))
        val stored = dataStore.data.first()
        assertThat(stored.listWeather.first { it.isActive }.name, `is`("Paris"))
        assertThat(viewModel.state.weather, notNullValue())
        assertThat(viewModel.state.error, nullValue())
    }

    @Test
    fun `loadWeatherFromCurrent loads the given city`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        val cordoba = WeatherLocal(name = "Cordoba", lat = 37.88, lon = -4.77)
        viewModel.loadWeatherFromCurrent(cordoba)
        advanceUntilIdle()

        assertThat(repository.lastLatitude, `is`(cordoba.lat))
        assertThat(repository.lastLongitude, `is`(cordoba.lon))
        assertThat(viewModel.state.weather, notNullValue())
    }

    @Test
    fun `loadWeatherFromSearch stores the city as active and loads it`() = runTest {
        val repository = FakeWeatherRepository()
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        viewModel.loadWeatherFromSearch(51.5, -0.12, "London")
        advanceUntilIdle()

        val stored = dataStore.data.first().listWeather
        assertThat(stored.size, `is`(2))
        assertThat(stored.first { it.isActive }.name, `is`("London"))
        assertThat(stored.first { it.name == "Madrid" }.isActive, `is`(false))
        assertThat(repository.lastLatitude, `is`(51.5))
    }

    // --- error mapping ---

    @Test
    fun `weather api error is mapped to its string resource and resets lastUpdate`() = runTest {
        val repository = FakeWeatherRepository().apply {
            weatherResult = Result.Error(DataError.Network.NO_INTERNET)
        }
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        viewModel.loadWeatherFromCurrent(madrid)
        advanceUntilIdle()

        assertThat(viewModel.state.isLoading, `is`(false))
        assertThat(viewModel.state.error, `is`(R.string.error_no_internet))
        assertThat(viewModel.state.weather, nullValue())
        assertThat(dataStore.data.first().lastUpdate, `is`(0L))
        // the forecast must not be requested if the current weather failed
        assertThat(repository.getForecastCallCount, `is`(0))
    }

    @Test
    fun `forecast api error is mapped to its string resource`() = runTest {
        val repository = FakeWeatherRepository().apply {
            forecastResult = Result.Error(DataError.Network.SERVER_ERROR)
        }
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        viewModel.loadWeatherFromCurrent(madrid)
        advanceUntilIdle()

        assertThat(viewModel.state.error, `is`(R.string.error_server_error))
        assertThat(viewModel.state.weather, nullValue())
        assertThat(dataStore.data.first().lastUpdate, `is`(0L))
    }

    @Test
    fun `refreshWeather on api error surfaces the mapped error`() = runTest {
        // #30 fixed by the use-case refactor: the RefreshDecision branches are
        // exclusive, so a failed load keeps the mapped error instead of being
        // overwritten by the generic refresh_error.
        val repository = FakeWeatherRepository().apply {
            weatherResult = Result.Error(DataError.Network.NO_INTERNET)
        }
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(repository = repository, dataStore = dataStore)

        viewModel.refreshWeather()
        advanceUntilIdle()

        assertThat(repository.getWeatherCallCount, `is`(1))
        assertThat(viewModel.state.isLoading, `is`(false))
        assertThat(viewModel.state.error, `is`(R.string.error_no_internet))
        assertThat(dataStore.data.first().lastUpdate, `is`(0L))
    }

    // --- offline cache ---

    @Test
    fun `network error with cached city shows cached weather flagged offline`() = runTest {
        val repository = FakeWeatherRepository().apply {
            weatherResult = Result.Error(DataError.Network.NO_INTERNET)
        }
        val cachedWeather = Weather(
            currentWeather = MockData.getCurrentWeatherList().first(),
            forecast = MockData.getForecastWeatherList().first()
        )
        val weatherCache = FakeWeatherCacheDataStore(
            WeatherCache().put(
                weatherCacheKey(madrid.lat, madrid.lon),
                CachedWeather(weather = cachedWeather, fetchedAt = 999L, lang = "en")
            )
        )
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(
            repository = repository,
            dataStore = dataStore,
            weatherCache = weatherCache
        )

        viewModel.loadWeatherFromCurrent(madrid)
        advanceUntilIdle()

        assertThat(viewModel.state.weather, notNullValue())
        assertThat(viewModel.state.isFromCache, `is`(true))
        assertThat(viewModel.state.lastFetchTime, `is`(999L))
        assertThat(viewModel.state.error, nullValue())
    }

    @Test
    fun `successful load flags the state as fresh data`() = runTest {
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(dataStore = dataStore)

        viewModel.loadWeatherFromCurrent(madrid)
        advanceUntilIdle()

        assertThat(viewModel.state.weather, notNullValue())
        assertThat(viewModel.state.isFromCache, `is`(false))
        assertThat(viewModel.state.lastFetchTime, notNullValue())
    }

    // --- permission dialog queue ---

    @Test
    fun `denied permission is queued once and dismissed in order`() {
        val viewModel = buildViewModel()
        val permission = "android.permission.ACCESS_COARSE_LOCATION"

        viewModel.onPermissionResult(permission, isGranted = false)
        viewModel.onPermissionResult(permission, isGranted = false)

        assertThat(viewModel.visiblePermissionDialogQueue.size, `is`(1))
        assertThat(viewModel.visiblePermissionDialogQueue.first(), `is`(permission))

        viewModel.dismissDialog()
        assertThat(viewModel.visiblePermissionDialogQueue.isEmpty(), `is`(true))
        // dismissing an empty queue must not throw
        viewModel.dismissDialog()
    }

    @Test
    fun `granted permission is not queued`() {
        val viewModel = buildViewModel()

        viewModel.onPermissionResult("android.permission.ACCESS_COARSE_LOCATION", isGranted = true)

        assertThat(viewModel.visiblePermissionDialogQueue.isEmpty(), `is`(true))
    }

    // --- misc state ---

    @Test
    fun `loadLocationWeather deactivates stored cities before using gps`() = runTest {
        val tracker = FakeLocationTracker(location = null)
        val dataStore = FakeDataStore(Settings(listWeather = persistentListOf(madrid)))
        val viewModel = buildViewModel(locationTracker = tracker, dataStore = dataStore)

        viewModel.loadLocationWeather()
        advanceUntilIdle()

        assertThat(dataStore.data.first().listWeather.none { it.isActive }, `is`(true))
        assertThat(viewModel.state.error, `is`(R.string.error_location))
    }

    @Test
    fun `initialStartUp clears startup flag and resets lastUpdate`() = runTest {
        val dataStore = FakeDataStore(Settings(lastUpdate = 12345L))
        val viewModel = buildViewModel(dataStore = dataStore)

        viewModel.initialStartUp(isFirstTime = true)
        advanceUntilIdle()

        assertThat(viewModel.isStartup.value, `is`(false))
        assertThat(viewModel.state.isLoading, `is`(false))
        assertThat(dataStore.data.first().lastUpdate, `is`(0L))
    }

    @Test
    fun `setPermissionAccepted persists the flag`() = runTest {
        val dataStore = FakeDataStore()
        val viewModel = buildViewModel(dataStore = dataStore)

        viewModel.setPermissionAccepted(true)

        assertThat(dataStore.data.first().permissionAccepted, `is`(true))
    }

    @Test
    fun `disableNotifications turns the daily notification off without touching the interval`() =
        runTest {
            val dataStore = FakeDataStore(
                Settings(notificationsEnabled = true, refreshIntervalMinutes = 30)
            )
            val viewModel = buildViewModel(dataStore = dataStore)

            viewModel.disableNotifications()
            advanceUntilIdle()

            assertThat(dataStore.data.first().notificationsEnabled, `is`(false))
            // the background sync must keep running: only the notification feature is off
            assertThat(dataStore.data.first().refreshIntervalMinutes, `is`(30))
        }
}
