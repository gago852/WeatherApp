package com.gago.weatherapp.data.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.gago.weatherapp.domain.location.LocationTracker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume

class DefaultLocationTracker @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val application: Application
) : LocationTracker {
    override suspend fun getCurrentLocation(): Location? {
        val hasAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val locationManager =
            application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!hasAccessCoarseLocationPermission || !isGpsEnable) {
            return null
        }

        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    LOCATION_REQUEST_INTERVAL_MS
                ).setMaxUpdates(1).build()

                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        locationClient.removeLocationUpdates(this)
                        if (cont.isActive) {
                            cont.resume(result.lastLocation)
                        }
                    }
                }

                cont.invokeOnCancellation {
                    locationClient.removeLocationUpdates(callback)
                }

                locationClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    Looper.getMainLooper()
                ).addOnFailureListener {
                    locationClient.removeLocationUpdates(callback)
                    if (cont.isActive) {
                        cont.resume(null)
                    }
                }
            }
        }
    }

    private companion object {
        private const val LOCATION_TIMEOUT_MS = 15_000L
        private const val LOCATION_REQUEST_INTERVAL_MS = 1_000L
    }
}
