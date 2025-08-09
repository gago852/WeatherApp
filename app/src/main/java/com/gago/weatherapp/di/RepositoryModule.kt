package com.gago.weatherapp.di

import com.gago.weatherapp.data.repository.WeatherRepositoryImpl
import com.gago.weatherapp.data.repository.PlacesRepositoryImpl
import com.gago.weatherapp.domain.repository.WeatherRepository
import com.gago.weatherapp.domain.repository.PlacesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(weatherRepositoryImpl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(placesRepositoryImpl: PlacesRepositoryImpl): PlacesRepository
}