package com.gago.weatherapp.fakes

import android.location.Location
import com.gago.weatherapp.domain.location.LocationTracker

class FakeLocationTracker(var location: Location? = null) : LocationTracker {

    var callCount = 0
        private set

    override suspend fun getCurrentLocation(): Location? {
        callCount++
        return location
    }
}
