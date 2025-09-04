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
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.mockito.MockedStatic
import org.mockito.Mockito

class WeatherRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var api: OpenWeatherMapApi
    private lateinit var repository: WeatherRepositoryImpl
    private lateinit var crashlyticsStatic: MockedStatic<FirebaseCrashlytics>

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val client = OkHttpClient.Builder().build()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()

        api = retrofit.create(OpenWeatherMapApi::class.java)

        // Mock static FirebaseCrashlytics.getInstance() to avoid Android deps in JVM tests
        crashlyticsStatic = Mockito.mockStatic(FirebaseCrashlytics::class.java)
        val mockCrashlytics = Mockito.mock(FirebaseCrashlytics::class.java)
        crashlyticsStatic.`when`<FirebaseCrashlytics> { FirebaseCrashlytics.getInstance() }
            .thenReturn(mockCrashlytics)
        Mockito.doNothing().`when`(mockCrashlytics).recordException(Mockito.any())

        repository = WeatherRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        try {
            crashlyticsStatic.close()
        } finally {
            server.shutdown()
        }
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
                .addHeader("Content-Type", "application/json")
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
                .addHeader("Content-Type", "application/json")
                .setBody("{\"cod\":401,\"message\":\"unauthorized\"}")
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
                .addHeader("Content-Type", "application/json")
                .setBody("{\"cod\":404,\"message\":\"not found\"}")
        )

        val result = repository.getForecastFiveDays(0.0, 0.0, "test", "en", "metric")
        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.NOT_FOUND, (result as Result.Error).error)
    }
}
