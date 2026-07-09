package com.gago.weatherapp.fakes

import androidx.datastore.core.DataStore
import com.gago.weatherapp.data.datastore.WeatherCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWeatherCacheDataStore(initial: WeatherCache = WeatherCache()) : DataStore<WeatherCache> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<WeatherCache> = state

    override suspend fun updateData(
        transform: suspend (t: WeatherCache) -> WeatherCache
    ): WeatherCache {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
