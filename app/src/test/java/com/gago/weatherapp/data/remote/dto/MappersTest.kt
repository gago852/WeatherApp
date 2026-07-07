package com.gago.weatherapp.data.remote.dto

import com.gago.weatherapp.data.remote.dto.common.Clouds
import com.gago.weatherapp.data.remote.dto.common.Coord
import com.gago.weatherapp.data.remote.dto.common.Rain
import com.gago.weatherapp.data.remote.dto.common.Snow
import com.gago.weatherapp.data.remote.dto.common.WeatherConditionDto
import com.gago.weatherapp.data.remote.dto.common.WeatherData
import com.gago.weatherapp.data.remote.dto.common.Wind
import com.gago.weatherapp.data.remote.dto.common.toDomain
import com.gago.weatherapp.data.remote.dto.common.toWeatherCondition
import com.gago.weatherapp.data.remote.dto.forecast.City
import com.gago.weatherapp.data.remote.dto.forecast.ForecastDto
import com.gago.weatherapp.data.remote.dto.forecast.PartOfTheDay
import com.gago.weatherapp.data.remote.dto.forecast.WeatherForecastDto
import com.gago.weatherapp.data.remote.dto.forecast.toDomain
import com.gago.weatherapp.data.remote.dto.forecast.toForecastFiveDays
import com.gago.weatherapp.data.remote.dto.forecast.toWeatherForecast
import com.gago.weatherapp.data.remote.dto.weather.SysDto
import com.gago.weatherapp.data.remote.dto.weather.WeatherDto
import com.gago.weatherapp.data.remote.dto.weather.toDayData
import com.gago.weatherapp.data.remote.dto.weather.toWeather
import com.gago.weatherapp.domain.model.WeatherTypeIcon
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MappersTest {

    private val weatherData = WeatherData(
        feelsLike = 23.42,
        humidity = 71,
        pressure = 1003,
        temp = 23.19,
        tempMax = 24.27,
        tempMin = 19.43
    )

    private val condition = WeatherConditionDto(
        id = 800,
        mainCondition = "Clear",
        description = "clear sky",
        icon = "01d"
    )

    private fun forecastEntry(dt: Int, dtTxt: String) = WeatherForecastDto(
        dt = dt,
        dtTxt = dtTxt,
        clouds = Clouds(all = 25),
        main = weatherData,
        pop = 0.35,
        partOfTheDay = PartOfTheDay(pod = "d"),
        visibility = 10000,
        wind = Wind(deg = 140, gust = 0.1, speed = 3.09),
        weather = listOf(condition),
        rain = Rain(oneHour = 1.0),
        snow = null
    )

    // --- WeatherConditionDto.toWeatherCondition ---

    @Test
    fun `toWeatherCondition maps fields and resolves the icon`() {
        val result = condition.toWeatherCondition()

        assertThat(result.id, `is`(800))
        assertThat(result.mainCondition, `is`("Clear"))
        assertThat(result.description, `is`("clear sky"))
        assertThat(result.icon, `is`<WeatherTypeIcon>(WeatherTypeIcon.ClearSkyDay))
    }

    @Test
    fun `toWeatherCondition resolves night and unknown icon codes`() {
        assertThat(
            condition.copy(icon = "10n").toWeatherCondition().icon,
            `is`<WeatherTypeIcon>(WeatherTypeIcon.RainNight)
        )
        assertThat(
            condition.copy(icon = "not-a-code").toWeatherCondition().icon,
            `is`<WeatherTypeIcon>(WeatherTypeIcon.ClearSkyDay)
        )
    }

    // --- SysDto.toDayData ---

    @Test
    fun `toDayData keeps sunrise and sunset as raw epochs`() {
        val sys = SysDto(
            country = "CA",
            sunrise = 1_717_666_920L,
            sunset = 1_717_727_880L
        )

        val dayData = sys.toDayData()

        assertThat(dayData.sunrise, `is`(1_717_666_920L))
        assertThat(dayData.sunset, `is`(1_717_727_880L))
    }

    // --- WeatherDto.toWeather ---

    @Test
    fun `toWeather maps every field of the current weather`() {
        val dto = WeatherDto(
            base = "stations",
            clouds = Clouds(all = 25),
            cod = 200,
            coord = Coord(lat = 45.5, lon = -73.55),
            dt = 0,
            id = 6077243,
            main = weatherData,
            visibility = 10000,
            name = "Montreal",
            rain = Rain(oneHour = 1.0, threeHour = 2.0),
            snow = Snow(oneHour = 0.5),
            sys = SysDto(country = "CA", sunrise = 0L, sunset = 43_200L),
            timezone = 0,
            weather = listOf(condition),
            wind = Wind(deg = 140, gust = 0.1, speed = 3.09)
        )

        val result = dto.toWeather()

        assertThat(result.id, `is`(6077243))
        assertThat(result.name, `is`("Montreal"))
        assertThat(result.timezone, `is`(0))
        assertThat(result.weatherData, `is`(weatherData.toDomain()))
        assertThat(result.wind, `is`(dto.wind.toDomain()))
        assertThat(result.rain, `is`(dto.rain?.toDomain()))
        assertThat(result.snow, `is`(dto.snow?.toDomain()))
        assertThat(result.visibility, `is`(10000))
        assertThat(result.clouds, `is`(25))
        assertThat(result.weatherConditions.icon, `is`<WeatherTypeIcon>(WeatherTypeIcon.ClearSkyDay))
        // raw epochs: the UI formats them with the locale active at render time
        assertThat(result.calculatedTime, `is`(0L))
        assertThat(result.dayData.sunrise, `is`(0L))
        assertThat(result.dayData.sunset, `is`(43_200L))
    }

    // --- WeatherForecastDto.toWeatherForecast ---

    @Test
    fun `toWeatherForecast maps fields and keeps the forecast time and offset raw`() {
        val dto = forecastEntry(dt = 4 * 86_400, dtTxt = "1970-01-05 00:00:00")

        val result = dto.toWeatherForecast(timeZoneOffset = -14_400L)

        assertThat(result.forecastTime, `is`(4 * 86_400L))
        assertThat(result.timeZoneOffset, `is`(-14_400L))
        assertThat(result.calculatedTimeFromServer, `is`("1970-01-05 00:00:00"))
        assertThat(result.mainData, `is`(weatherData.toDomain()))
        assertThat(result.probabilityOfPrecipitation, `is`(0.35))
        assertThat(result.partOfTheDay.pod, `is`("d"))
        assertThat(result.visibility, `is`(10000))
        assertThat(result.wind, `is`(dto.wind.toDomain()))
        assertThat(result.rain, `is`(dto.rain?.toDomain()))
        assertThat(result.snow, `is`(dto.snow?.toDomain()))
        assertThat(result.weatherCondition.id, `is`(800))
    }

    // --- ForecastDto.toForecastFiveDays ---

    private val city = City(
        id = 6077243,
        name = "Montreal",
        country = "CA",
        population = 5,
        sunrise = 123,
        sunset = 456,
        timezone = 0,
        coord = Coord(lat = 45.5, lon = -73.55)
    )

    @Test
    fun `toForecastFiveDays groups by day and picks the entry closest to noon`() {
        // 6 days, each with entries at 09:00, 12:00 and 15:00 local time
        val entries = (0 until 6).flatMap { day ->
            listOf(9, 12, 15).map { hour ->
                forecastEntry(
                    dt = day * 86_400 + hour * 3_600,
                    dtTxt = "day$day-h$hour"
                )
            }
        }
        val dto = ForecastDto(
            cod = "200",
            message = 0,
            city = city,
            count = entries.size,
            listWeatherForecast = entries
        )

        val result = dto.toForecastFiveDays()

        assertThat(result.city, `is`(city.toDomain()))
        assertThat(result.forecastCount, `is`(entries.size))
        // only the first five days survive, one entry per day
        assertThat(result.listForecastWeather.size, `is`(5))
        result.listForecastWeather.forEachIndexed { day, forecast ->
            assertThat(forecast.calculatedTimeFromServer, `is`("day$day-h12"))
        }
    }

    @Test
    fun `toForecastFiveDays exposes the first eight slots as the hourly forecast`() {
        val entries = (0 until 12).map { slot ->
            forecastEntry(dt = slot * 3 * 3_600, dtTxt = "slot$slot")
        }
        val dto = ForecastDto(
            cod = "200",
            message = 0,
            city = city.copy(timezone = -14_400),
            count = entries.size,
            listWeatherForecast = entries
        )

        val result = dto.toForecastFiveDays()

        assertThat(result.hourlyForecast.size, `is`(8))
        result.hourlyForecast.forEachIndexed { index, slot ->
            assertThat(slot.calculatedTimeFromServer, `is`("slot$index"))
            assertThat(slot.forecastTime, `is`(index * 3 * 3_600L))
            assertThat(slot.timeZoneOffset, `is`(-14_400L))
        }
    }

    @Test
    fun `toForecastFiveDays picks the nearest entry when noon is missing`() {
        val entries = listOf(
            forecastEntry(dt = 0, dtTxt = "midnight"),          // 00:00, 12h from noon
            forecastEntry(dt = 21 * 3_600, dtTxt = "evening")   // 21:00, 9h from noon
        )
        val dto = ForecastDto(
            cod = "200",
            message = 0,
            city = city,
            count = entries.size,
            listWeatherForecast = entries
        )

        val result = dto.toForecastFiveDays()

        assertThat(result.listForecastWeather.size, `is`(1))
        assertThat(result.listForecastWeather.first().calculatedTimeFromServer, `is`("evening"))
    }

    @Test
    fun `toForecastFiveDays groups days using the city time zone offset`() {
        // UTC+6: 22:00 UTC belongs to the next local day
        val entries = listOf(
            forecastEntry(dt = 12 * 3_600, dtTxt = "day1"),
            forecastEntry(dt = 22 * 3_600, dtTxt = "day2-local")
        )
        val dto = ForecastDto(
            cod = "200",
            message = 0,
            city = city.copy(timezone = 21_600),
            count = entries.size,
            listWeatherForecast = entries
        )

        val result = dto.toForecastFiveDays()

        assertThat(result.listForecastWeather.size, `is`(2))
        assertThat(result.listForecastWeather[0].calculatedTimeFromServer, `is`("day1"))
        assertThat(result.listForecastWeather[1].calculatedTimeFromServer, `is`("day2-local"))
    }
}
