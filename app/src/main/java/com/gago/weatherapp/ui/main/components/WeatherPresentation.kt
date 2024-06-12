package com.gago.weatherapp.ui.main.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gago.weatherapp.data.remote.dto.common.Rain
import com.gago.weatherapp.data.remote.dto.common.Snow
import com.gago.weatherapp.data.remote.dto.common.WeatherData
import com.gago.weatherapp.data.remote.dto.common.Wind
import com.gago.weatherapp.domain.model.DayData
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.model.WeatherCondition
import com.gago.weatherapp.domain.model.WeatherTypeIcon
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.MeasureUnit
import com.gago.weatherapp.ui.utils.capitalizeWords
import kotlin.math.roundToInt

@Composable
fun WeatherPresentation(weather: Weather, measureUnit: MeasureUnit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        OutlinedCard(
            modifier = Modifier.padding(8.dp),
            colors = CardDefaults.cardColors().copy(
                containerColor =
                MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = weather.weatherConditions.icon.weatherIcon),
                        contentDescription = weather.weatherConditions.description,
                        modifier = Modifier
                            .size(130.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = weather.weatherData.temp.roundToInt().toString().plus("°"),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }


                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    Text(
                        text = weather.weatherConditions.description.capitalizeWords(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Feels like ${weather.weatherData.feelsLike.roundToInt()}°",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Max ${weather.weatherData.tempMax.roundToInt()}° Min ${weather.weatherData.tempMin.roundToInt()}°",
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.padding(8.dp), colors = CardDefaults.cardColors()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "wind", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${
                                weather.wind.deg.toString().plus("°")
                            } ${weather.wind.speed?.toString() ?: "0"} ${measureUnit.windSpeedText} "
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp), color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "wind gust", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${weather.wind.gust?.toString() ?: "0"} ${measureUnit.windSpeedText}"
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "humidity", style = MaterialTheme.typography.titleMedium)
                        Text(text = weather.weatherData.humidity.toString().plus("%"))
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "preasure", style = MaterialTheme.typography.titleMedium)
                        Text(text = weather.weatherData.pressure.toString().plus("hPa"))
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "clouds", style = MaterialTheme.typography.titleMedium)
                        Text(text = weather.clouds.toString().plus("%"))
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "sunrise", style = MaterialTheme.typography.titleMedium)
                        Text(text = weather.dayData.sunrise)
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "sunset", style = MaterialTheme.typography.titleMedium)
                        Text(text = weather.dayData.sunset)
                    }
                    weather.rain?.let { rain ->
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            color = Color.Gray
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "rain", style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Column(
                                modifier = Modifier,
                                horizontalAlignment = Alignment.End
                            ) {
                                rain.oneHour?.let {
                                    Text(text = "Last hour: ${it.toString().plus("mm")}")
                                }
                                rain.threeHour?.let {
                                    Text(text = "Last three hours: ${it.toString().plus("mm")}")
                                }
                            }
                        }
                    }
                    weather.snow?.let { snow ->
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            color = Color.Gray
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "snow", style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                snow.oneHour?.let {
                                    Text(text = "Last hour: ${it.toString().plus("mm")}")
                                }
                                snow.threeHour?.let {
                                    Text(text = "Last three hours: ${it.toString().plus("mm")}")
                                }
                            }
                        }
                    }
                }
            }
        }
        Text(
            text = "Last update: ${weather.calculatedTime}",
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp, top = 8.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun WeatherPresentationPreview() {
    WeatherAppTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            WeatherPresentation(
                measureUnit = MeasureUnit.METRIC,
                weather = Weather(
                    id = 6077243,
                    name = "Montreal",
                    timezone = -14400,
                    dayData =
                    DayData(
                        sunrise = "01:02 AM",
                        sunset = "05:58 PM",
                    ),
                    calculatedTime = "2024-06-06 01:29:58",
                    weatherConditions = WeatherCondition(
                        description = "cielo claro y nubes dispersas",
                        icon = WeatherTypeIcon.ClearSkyNight,
                        id = 800,
                        mainCondition = "Clear"
                    ),
                    weatherData = WeatherData(
                        feelsLike = 23.42,
                        humidity = 71,
                        pressure = 1003,
                        temp = 23.19,
                        tempMax = 24.27,
                        tempMin = 19.43
                    ),
                    wind = Wind(deg = 140, gust = 0.1, speed = 3.09),
                    visibility = 5,
                    clouds = 25,
                    rain = Rain(oneHour = 1.0, threeHour = 2.0),
                    snow = Snow(oneHour = 1.0, threeHour = 3.0)
                )
            )
        }
    }
}

@Preview(
    showBackground = true, showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun WeatherPresentationDarkPreview() {
    WeatherAppTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            WeatherPresentation(
                measureUnit = MeasureUnit.METRIC,
                weather = Weather(
                    id = 6077243,
                    name = "Montreal",
                    timezone = -14400,
                    dayData =
                    DayData(
                        sunrise = "01:02 AM",
                        sunset = "05:58 PM",
                    ),
                    calculatedTime = "2024-06-06 01:29:58",
                    weatherConditions = WeatherCondition(
                        description = "cielo claro",
                        icon = WeatherTypeIcon.ClearSkyNight,
                        id = 800,
                        mainCondition = "Clear"
                    ),
                    weatherData = WeatherData(
                        feelsLike = 23.42,
                        humidity = 71,
                        pressure = 1003,
                        temp = 23.19,
                        tempMax = 24.27,
                        tempMin = 19.43
                    ),
                    wind = Wind(deg = 140, gust = 0.1, speed = 3.09),
                    visibility = 5,
                    clouds = 25,
                    rain = Rain(oneHour = 1.0, threeHour = 2.0),
                    snow = Snow(oneHour = 1.0, threeHour = 3.0)
                )
            )
        }
    }
}