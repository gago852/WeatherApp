package com.gago.weatherapp.fakes

import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.utils.DataError
import com.gago.weatherapp.domain.utils.Result
import com.gago.weatherapp.ui.utils.MockData

class FakeWeatherRepository : WeatherRepository {

    var weatherResult: Result<CurrentWeather, DataError.Network> =
        Result.Success(MockData.getCurrentWeatherList().first())
    var forecastResult: Result<Forecast, DataError.Network> =
        Result.Success(MockData.getForecastWeatherList().first())

    var getWeatherCallCount = 0
        private set
    var getForecastCallCount = 0
        private set
    var lastLatitude: Double? = null
        private set
    var lastLongitude: Double? = null
        private set

    override suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Result<CurrentWeather, DataError.Network> {
        getWeatherCallCount++
        lastLatitude = latitude
        lastLongitude = longitude
        return weatherResult
    }

    override suspend fun getForecastFiveDays(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        lang: String,
        units: String
    ): Result<Forecast, DataError.Network> {
        getForecastCallCount++
        return forecastResult
    }
}
