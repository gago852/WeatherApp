package com.gago.weatherapp.domain.usecase

import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.fakes.FakeDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
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
    private lateinit var useCase: GetWeatherUseCase

    private val currentWeather: CurrentWeather = mock()
    private val forecast: Forecast = mock()

    @Before
    fun setUp() {
        repository = mock()
        dataStore = FakeDataStore(Settings(lastUpdate = 123L))
        useCase = GetWeatherUseCase(repository, dataStore)
    }

    private suspend fun invoke() = useCase(10.96, -74.79, "apiKey", "en", "metric")

    @Test
    fun bothCallsSucceed_returnsWeatherAndStampsLastUpdate() = runTest {
        whenever(repository.getWeather(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(currentWeather))
        whenever(repository.getForecastFiveDays(any(), any(), any(), any(), any()))
            .thenReturn(Result.Success(forecast))

        val result = invoke()

        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        result as Result.Success
        assertThat(result.data.currentWeather, `is`(currentWeather))
        assertThat(result.data.forecast, `is`(forecast))
        assertThat(dataStore.data.first().lastUpdate, `is`(greaterThan(123L)))
        assertThat(dataStore.data.first().lastLangUsed, `is`("en"))
    }

    @Test
    fun currentWeatherFails_returnsErrorWithoutCallingForecast_andResetsLastUpdate() = runTest {
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
    fun forecastFails_returnsErrorAndResetsLastUpdate() = runTest {
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
}
