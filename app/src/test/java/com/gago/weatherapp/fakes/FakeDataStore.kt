package com.gago.weatherapp.fakes

import androidx.datastore.core.DataStore
import com.gago.weatherapp.data.datastore.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDataStore(initial: Settings = Settings()) : DataStore<Settings> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Settings> = state

    override suspend fun updateData(transform: suspend (t: Settings) -> Settings): Settings {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
