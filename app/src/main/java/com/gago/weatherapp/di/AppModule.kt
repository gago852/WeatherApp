package com.gago.weatherapp.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.SettingsSerializer
import com.gago.weatherapp.data.remote.OpenWeatherMapApi
import com.gago.weatherapp.data.remote.interceptor.LiveNetworkMonitor
import com.gago.weatherapp.data.remote.interceptor.NetworkMonitor
import com.gago.weatherapp.data.remote.interceptor.NetworkMonitorInterceptor
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Singleton
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.gago.weatherapp.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherApi(networkMonitor: NetworkMonitor): OpenWeatherMapApi {

        val monitorClient = OkHttpClient.Builder()
            .addInterceptor(NetworkMonitorInterceptor(networkMonitor))
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(monitorClient)
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

    @Provides
    @Singleton
    fun provideNetworkMonitor(app: Application): NetworkMonitor {
        return LiveNetworkMonitor(app)
    }

    @Provides
    @Singleton
    fun providePlacesClient(app: Application): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(app, BuildConfig.PLACES_API_KEY)
        }
        return Places.createClient(app)
    }
}