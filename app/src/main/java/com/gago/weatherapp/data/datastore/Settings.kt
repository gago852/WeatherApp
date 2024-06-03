package com.gago.weatherapp.data.datastore

import com.gago.weatherapp.ui.utils.MeasureUnit
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val unitOfMeasurement: MeasureUnit = MeasureUnit.METRIC,
    val permissionAccepted: Boolean = false,
    val lastUpdate: Long = 0,

    @Serializable(with = MyPersistentListSerializer::class)
    val listWeather: PersistentList<WeatherLocal> = persistentListOf()
)