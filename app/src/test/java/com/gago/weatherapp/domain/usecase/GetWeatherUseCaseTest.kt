package com.gago.weatherapp.domain.usecase

import com.gago.weatherapp.data.datastore.CachedWeather
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherCache
import com.gago.weatherapp.data.datastore.weatherCacheKey
import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.fakes.FakeDataStore
import com.gago.weatherapp.fakes.FakeWeatherCacheDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetWeatherUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var dataStore: FakeDataStore
    private lateinit var weatherCache: FakeWeatherCacheDataStore
    private lateinit var useCase: GetWeatherUseCase

    private val currentWeather: CurrentWeather = mock()
    private val forecast: Forecast = mock()

    private val latitude = 10.96
    private val longitude = -74.79
    private val cacheKey = weatherCacheKey(latitude, longitude)

    @Before
    fun setUp() {
        repository = mock()
        dataStore = FakeDataStore(Settings(lastUpdate = 123L))
        weatherCache = FakeWeatherCacheDataStore()
        useCase = GetWeatherUseCase(repository, dataStore, weatherCache)
    }

    private suspend fun invoke() = useCase(latitude, longitude, "apiKey", "en", "metric")

    private fun cachedEntry(fetchedAt: Long = 999L) = CachedWeather(
        weather = Weather(currentWeather = currentWeather, forecast = forecast),
        fetchedAt = fetchedAt,
        lang = "en"
    )

    @Test
    fun bothCallsSucceed_returnsWeatherStampsLastUpdateAndWritesCache() = runTest {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(currentWeather))
        whenever(repository.getForecastFiveDays(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(forecast))

        val result = invoke()

        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        result as Result.Success
        assertThat(result.data.weather.currentWeather, `is`(currentWeather))
        assertThat(result.data.weather.forecast, `is`(forecast))
        assertThat(result.data.isFromCache, `is`(false))
        assertThat(dataStore.data.first().lastUpdate, `is`(greaterThan(123L)))
        assertThat(dataStore.data.first().lastLangUsed, `is`("en"))
        val cached = weatherCache.data.first().entries[cacheKey]
        assertThat(cached, `is`(notNullValue()))
        assertThat(cached!!.weather.currentWeather, `is`(currentWeather))
        assertThat(cached.lang, `is`("en"))
    }

    @Test
    fun currentWeatherFails_withoutCache_returnsErrorWithoutCallingForecast_andResetsLastUpdate() =
        runTest {
            whenever(repository.getWeather(any(), any(), any(), any(), any()))
                .thenReturn(Result.Error(DataError.Network.UNAUTHORIZED))

            val result = invoke()

            assertThat(result, `is`(instanceOf(Result.Error::class.java)))
            result as Result.Error
            assertThat(result.error as DataError, `is`(DataError.Network.UNAUTHORIZED))
            verify(repository, never()).getForecastFiveDays(any(), any(), any(), any(), any())
            assertThat(dataStore.data.first().lastUpdate, `is`(0L))
        }

    @Test
    fun forecastFails_withoutCache_returnsErrorAndResetsLastUpdate() = runTest {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(currentWeather))
        whenever(repository.getForecastFiveDays(any(), any(), any(), any(), any()))
            .thenReturn(Result.Error(DataError.Network.NO_INTERNET))

        val result = invoke()

        assertThat(result, `is`(instanceOf(Result.Error::class.java)))
        result as Result.Error
        assertThat(result.error as DataError, `is`(DataError.Network.NO_INTERNET))
        assertThat(dataStore.data.first().lastUpdate, `is`(0L))
    }

    // --- offline cache fallback ---

    @Test
    fun networkFails_withCachedCity_returnsCachedWeatherFlaggedAsStale() = runTest {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Error(DataError.Network.NO_INTERNET))
        weatherCache.updateData { it.put(cacheKey, cachedEntry(fetchedAt = 999L)) }

        val result = invoke()

        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        result as Result.Success
        assertThat(result.data.isFromCache, `is`(true))
        assertThat(result.data.fetchedAt, `is`(999L))
        assertThat(result.data.weather.currentWeather, `is`(currentWeather))
        // the throttle is still reset so the next refresh retries the network
        assertThat(dataStore.data.first().lastUpdate, `is`(0L))
    }

    @Test
    fun networkFails_withAnotherCityCached_returnsError() = runTest {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Error(DataError.Network.NO_INTERNET))
        weatherCache.updateData { it.put(weatherCacheKey(0.0, 0.0), cachedEntry()) }

        val result = invoke()

        assertThat(result, `is`(instanceOf(Result.Error::class.java)))
    }

    @Test
    fun successfulFetch_replacesTheCachedEntryForTheSameCity() = runTest {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(currentWeather))
        whenever(repository.getForecastFiveDays(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(forecast))
        weatherCache.updateData { it.put(cacheKey, cachedEntry(fetchedAt = 1L)) }

        invoke()

        val entries = weatherCache.data.first().entries
        assertThat(entries.size, `is`(1))
        assertThat(entries[cacheKey]!!.fetchedAt, `is`(greaterThan(1L)))
    }

    @Test
    fun cache_evictsOldestEntriesBeyondTheLimit() = runTest {
        var cache = WeatherCache()
        repeat(WeatherCache.MAX_ENTRIES + 3) { index ->
            cache = cache.put(
                weatherCacheKey(index.toDouble(), 0.0),
                cachedEntry(fetchedAt = index.toLong())
            )
        }

        assertThat(cache.entries.size, `is`(WeatherCache.MAX_ENTRIES))
        // the oldest entries were evicted
        assertThat(cache.entries.containsKey(weatherCacheKey(0.0, 0.0)), `is`(false))
        assertThat(
            cache.entries.containsKey(
                weatherCacheKey((WeatherCache.MAX_ENTRIES + 2).toDouble(), 0.0)
            ),
            `is`(true)
        )
    }
}
