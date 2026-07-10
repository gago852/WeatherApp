package com.gago.weatherapp.data.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.gago.weatherapp.rules.MainDispatcherRule
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultLocationTrackerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var contextCompatStatic: MockedStatic<ContextCompat>
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var application: Application
    private lateinit var locationManager: LocationManager
    private lateinit var tracker: DefaultLocationTracker

    @Before
    fun setUp() {
        contextCompatStatic = Mockito.mockStatic(ContextCompat::class.java)
        locationClient = mock()
        locationManager = mock()
        application = mock()

        whenever(application.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager)
        whenever(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
        whenever(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true)

        tracker = DefaultLocationTracker(locationClient, application)
    }

    @After
    fun tearDown() {
        contextCompatStatic.close()
    }

    private fun grantPermission() {
        contextCompatStatic.`when`<Int> {
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }.thenReturn(PackageManager.PERMISSION_GRANTED)
    }

    private fun denyPermission() {
        contextCompatStatic.`when`<Int> {
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }.thenReturn(PackageManager.PERMISSION_DENIED)
    }

    @Test
    fun getCurrentLocation_updatesNeverArrive_returnsNullAfterTimeout() = runTest {
        grantPermission()
        val task = mock<Task<Void>>()
        whenever(
            locationClient.requestLocationUpdates(
                any<LocationRequest>(),
                any<LocationCallback>(),
                anyOrNull<Looper>()
            )
        ).thenReturn(task)

        val result = tracker.getCurrentLocation()

        assertNull(result)
    }

    @Test
    fun getCurrentLocation_callbackReceivesResult_returnsLocation() = runTest {
        grantPermission()
        val expectedLocation = mock<Location>()
        val locationResult = mock<LocationResult>()
        whenever(locationResult.lastLocation).thenReturn(expectedLocation)

        val task = mock<Task<Void>>()
        whenever(
            locationClient.requestLocationUpdates(
                any<LocationRequest>(),
                any<LocationCallback>(),
                anyOrNull<Looper>()
            )
        ).thenAnswer { invocation ->
            val callback = invocation.getArgument<LocationCallback>(1)
            callback.onLocationResult(locationResult)
            task
        }

        val result = tracker.getCurrentLocation()

        assertEquals(expectedLocation, result)
    }

    @Test
    fun getCurrentLocation_permissionDenied_returnsNullImmediately() = runTest {
        denyPermission()

        val result = tracker.getCurrentLocation()

        assertNull(result)
        Mockito.verifyNoInteractions(locationClient)
    }

    @Test
    fun getCurrentLocation_providersDisabled_returnsNullImmediately() = runTest {
        grantPermission()
        whenever(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(false)
        whenever(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(false)

        val result = tracker.getCurrentLocation()

        assertNull(result)
        Mockito.verifyNoInteractions(locationClient)
    }
}
