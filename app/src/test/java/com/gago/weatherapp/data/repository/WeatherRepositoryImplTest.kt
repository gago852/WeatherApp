package com.gago.weatherapp.data.repository

import com.gago.weatherapp.data.remote.OpenWeatherMapApi
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WeatherRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OpenWeatherMapApi
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

        api = retrofit.create(OpenWeatherMapApi::class.java)
        repository = WeatherRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun readResource(name: String): String {
        val stream = javaClass.classLoader!!.getResourceAsStream(name)
            ?: error("Resource not found: $name")
        return stream.bufferedReader().use { it.readText() }
    }

    @Test
    fun getWeather_success_returnsCurrentWeather() = runTest {
        val body = readResource("mock_weather_current.json")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
        )

        val result = repository.getWeather(
            latitude = 45.5017,
            longitude = -73.5673,
            apiKey = "test",
            lang = "en",
            units = "metric"
        )

        assertTrue(result is Result.Success)
        val weather = (result as Result.Success).data
        assertEquals("Montreal", weather.name)
        assertEquals(6077243, weather.id)
        assertEquals(273.15, weather.weatherData.temp, 0.0)
        assertEquals(75, weather.clouds)
    }

    @Test
    fun getForecast_success_returnsFiveItems() = runTest {
        val body = readResource("mock_forecast_5days.json")
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body)
        )

        val result = repository.getForecastFiveDays(
            latitude = 45.5017,
            longitude = -73.5673,
            apiKey = "test",
            lang = "en",
            units = "metric"
        )

        assertTrue(result is Result.Success)
        val forecast = (result as Result.Success).data
        assertEquals("Montreal", forecast.city.name)
        assertEquals(5, forecast.forecastCount)
        assertEquals(5, forecast.listForecastWeather.size)
    }

    @Test
    fun getWeather_unauthorized_mapsError() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
        )

        val result = repository.getWeather(0.0, 0.0, "bad", "en", "metric")
        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.UNAUTHORIZED, (result as Result.Error).error)
    }

    @Test
    fun getForecast_notFound_mapsError() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(404)
        )

        val result = repository.getForecastFiveDays(0.0, 0.0, "test", "en", "metric")
        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.NOT_FOUND, (result as Result.Error).error)
    }
}
