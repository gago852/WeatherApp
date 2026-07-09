package com.gago.weatherapp.domain.usecase

import com.gago.weatherapp.data.datastore.CachedWeather
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherLocal
import com.gago.weatherapp.data.datastore.weatherCacheKey
import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.fakes.FakeDataStore
import com.gago.weatherapp.fakes.FakeWeatherCacheDataStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SyncActiveCityUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var dataStore: FakeDataStore
    private lateinit var weatherCache: FakeWeatherCacheDataStore
    private lateinit var useCase: SyncActiveCityUseCase

    private val currentWeather: CurrentWeather = mock()
    private val forecast: Forecast = mock()

    private val activeCity = WeatherLocal(
        name = "Barranquilla",
        lat = 10.96,
        lon = -74.79,
        isActive = true,
        isGps = false
    )

    @Before
    fun setUp() {
        repository = mock()
        weatherCache = FakeWeatherCacheDataStore()
    }

    private fun build(settings: Settings) {
        dataStore = FakeDataStore(settings)
        useCase = SyncActiveCityUseCase(
            GetWeatherUseCase(repository, dataStore, weatherCache),
            dataStore
        )
    }

    private suspend fun stubSuccess() {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(currentWeather))
        whenever(repository.getForecastFiveDays(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(forecast))
    }

    @Test
    fun `no active city returns NoActiveCity without calling the api`() = runTest {
        build(Settings())

        val outcome = useCase("key", "en")

        assertThat(
            outcome,
            `is`(instanceOf(SyncActiveCityUseCase.Outcome.NoActiveCity::class.java))
        )
    }

    @Test
    fun `fresh fetch returns Refreshed and stamps the notification when due`() = runTest {
        stubSuccess()
        build(
            Settings(
                listWeather = persistentListOf(activeCity),
                notificationsEnabled = true,
                lastNotificationTime = 0L
            )
        )

        val now = SyncActiveCityUseCase.NOTIFICATION_PERIOD_MS + 1L
        val outcome = useCase("key", "en", now = now)

        assertThat(outcome, `is`(instanceOf(SyncActiveCityUseCase.Outcome.Refreshed::class.java)))
        outcome as SyncActiveCityUseCase.Outcome.Refreshed
        assertThat(outcome.notify, `is`(true))
        assertThat(dataStore.data.first().lastNotificationTime, `is`(now))
    }

    @Test
    fun `stored in-app language wins over the fallback language`() = runTest {
        stubSuccess()
        build(Settings(listWeather = persistentListOf(activeCity), language = "es"))

        useCase("key", "en")

        verify(repository).getWeather(any(), any(), any(), eq("es"), any())
    }

    @Test
    fun `empty in-app language uses the fallback language`() = runTest {
        stubSuccess()
        build(Settings(listWeather = persistentListOf(activeCity)))

        useCase("key", "fr")

        verify(repository).getWeather(any(), any(), any(), eq("fr"), any())
    }

    @Test
    fun `notification is not requested again within the daily window`() = runTest {
        stubSuccess()
        val now = 10_000_000_000L
        build(
            Settings(
                listWeather = persistentListOf(activeCity),
                notificationsEnabled = true,
                lastNotificationTime = now - 60_000L
            )
        )

        val outcome = useCase("key", "en", now = now)

        outcome as SyncActiveCityUseCase.Outcome.Refreshed
        assertThat(outcome.notify, `is`(false))
    }

    @Test
    fun `notifications disabled never request a notification`() = runTest {
        stubSuccess()
        build(
            Settings(
                listWeather = persistentListOf(activeCity),
                notificationsEnabled = false,
                lastNotificationTime = 0L
            )
        )

        val outcome = useCase("key", "en", now = 999_999_999L)

        outcome as SyncActiveCityUseCase.Outcome.Refreshed
        assertThat(outcome.notify, `is`(false))
    }

    @Test
    fun `network failure without cache returns Failed`() = runTest {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Error(DataError.Network.NO_INTERNET))
        build(Settings(listWeather = persistentListOf(activeCity)))

        val outcome = useCase("key", "en")

        assertThat(outcome, `is`(instanceOf(SyncActiveCityUseCase.Outcome.Failed::class.java)))
    }

    @Test
    fun `cache fallback counts as Failed so the worker retries`() = runTest {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Error(DataError.Network.NO_INTERNET))
        weatherCache.updateData {
            it.put(
                weatherCacheKey(activeCity.lat, activeCity.lon),
                CachedWeather(
                    weather = Weather(currentWeather = currentWeather, forecast = forecast),
                    fetchedAt = 1L,
                    lang = "en"
                )
            )
        }
        build(Settings(listWeather = persistentListOf(activeCity)))

        val outcome = useCase("key", "en")

        assertThat(outcome, `is`(instanceOf(SyncActiveCityUseCase.Outcome.Failed::class.java)))
    }
}
