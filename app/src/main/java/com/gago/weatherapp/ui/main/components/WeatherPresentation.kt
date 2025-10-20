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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import com.gago.weatherapp.R
import com.gago.weatherapp.domain.model.CurrentWeather
import com.gago.weatherapp.domain.model.Forecast
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.ui.theme.WeatherAppTheme
import com.gago.weatherapp.ui.utils.MeasureUnit
import com.gago.weatherapp.ui.utils.PreviewWeatherListProvider
import com.gago.weatherapp.ui.utils.capitalizeWords
import kotlin.math.roundToInt

@Composable
fun WeatherPresentation(
    currentWeather: CurrentWeather,
    fiveDaysForecast: Forecast,
    measureUnit: MeasureUnit
) {
    val screenWidth = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        OutlinedCard(
            modifier = Modifier
                .padding(8.dp)
                .align(alignment = Alignment.CenterHorizontally),
            colors = CardDefaults.cardColors().copy(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Center
            ) {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = currentWeather.weatherConditions.icon.weatherIcon),
                        contentDescription = currentWeather.weatherConditions.description,
                        modifier = Modifier
                            .size(130.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = currentWeather.weatherData.temp.roundToInt().toString().plus("°"),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }


                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    Text(
                        text = currentWeather.weatherConditions.description.capitalizeWords(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${stringResource(R.string.feels_like_text)} ${currentWeather.weatherData.feelsLike.roundToInt()}°",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Max ${currentWeather.weatherData.tempMax.roundToInt()}° Min ${currentWeather.weatherData.tempMin.roundToInt()}°",
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        //List of 5 days
        Text(
            text = stringResource(R.string.five_days_forecast_text),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            items(fiveDaysForecast.listForecastWeather) {
                ForecastItem(weatherForecast = it)
            }
        }


        Spacer(modifier = Modifier.height(8.dp))
        ElevatedCard(
            modifier = Modifier
                .padding(8.dp)
                .align(alignment = Alignment.CenterHorizontally),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(
                        when (screenWidth) {
                            WindowWidthSizeClass.COMPACT -> 1.0f
                            else -> 0.5f
                        }
                    )

            ) {
                Text(
                    text = stringResource(R.string.details_text),
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
                        Text(
                            text = stringResource(R.string.wind_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${
                                currentWeather.wind.deg.toString().plus("°")
                            } ${currentWeather.wind.speed?.toString() ?: "0"} ${measureUnit.windSpeedText} "
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
                        Text(
                            text = stringResource(R.string.wind_gust_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${currentWeather.wind.gust?.toString() ?: "0"} ${measureUnit.windSpeedText}"
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
                        Text(
                            text = stringResource(R.string.humidity_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = currentWeather.weatherData.humidity.toString().plus("%"))
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
                        Text(
                            text = stringResource(R.string.pressure_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = currentWeather.weatherData.pressure.toString().plus("hPa"))
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
                        Text(
                            text = stringResource(R.string.clouds_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = currentWeather.clouds.toString().plus("%"))
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
                        Text(
                            text = stringResource(R.string.sunrise_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = currentWeather.dayData.sunrise)
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
                        Text(
                            text = stringResource(R.string.sunset_text),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(text = currentWeather.dayData.sunset)
                    }
                    currentWeather.rain?.let { rain ->
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
                                text = stringResource(R.string.rain_text),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Column(
                                modifier = Modifier,
                                horizontalAlignment = Alignment.End
                            ) {
                                rain.oneHour?.let {
                                    Text(
                                        text = "${stringResource(R.string.last_hour_text)} ${
                                            it.toString().plus("mm")
                                        }"
                                    )
                                }
                                rain.threeHour?.let {
                                    Text(
                                        text = "${stringResource(R.string.last_three_hours_text)}  ${
                                            it.toString().plus("mm")
                                        }"
                                    )
                                }
                            }
                        }
                    }
                    currentWeather.snow?.let { snow ->
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
                                text = stringResource(R.string.snow_text),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                snow.oneHour?.let {
                                    Text(
                                        text = "${stringResource(R.string.last_hour_text)} ${
                                            it.toString().plus("mm")
                                        }"
                                    )
                                }
                                snow.threeHour?.let {
                                    Text(
                                        text = "${stringResource(R.string.last_three_hours_text)}  ${
                                            it.toString().plus("mm")
                                        }"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Text(
            text = "${stringResource(R.string.last_updated_text)} ${currentWeather.calculatedTime}",
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp, top = 8.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun WeatherPresentationPreview(
    @PreviewParameter(PreviewWeatherListProvider::class) weather: Weather
) {
    WeatherAppTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            WeatherPresentation(
                measureUnit = MeasureUnit.METRIC,
                currentWeather = weather.currentWeather,
                fiveDaysForecast = weather.forecast
            )
        }
    }
}

@Preview(
    showBackground = true, showSystemUi = false,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun WeatherPresentationDarkPreview(
    @PreviewParameter(PreviewWeatherListProvider::class) weather: Weather
) {
    WeatherAppTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            WeatherPresentation(
                measureUnit = MeasureUnit.METRIC,
                currentWeather = weather.currentWeather,
                fiveDaysForecast = weather.forecast
            )
        }
    }
}
