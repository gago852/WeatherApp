package com.gago.weatherapp.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.SettingsSerializer
import com.gago.weatherapp.data.remote.OpenWeatherMapApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherApi(): OpenWeatherMapApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    @Provides
    @Singleton
    fun providePreferenceDataStore(app: Application): DataStore<Settings> {
        return DataStoreFactory.create(
            serializer = SettingsSerializer,
            produceFile = { app.dataStoreFile("app-settings.json") },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }
}