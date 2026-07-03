package com.gago.weatherapp.data.remote.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LiveNetworkMonitorTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var monitor: LiveNetworkMonitor

    @Before
    fun setUp() {
        context = mock()
        connectivityManager = mock()
        network = mock()
        whenever(context.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(connectivityManager)
        monitor = LiveNetworkMonitor(context)
    }

    private fun capabilities(internet: Boolean, validated: Boolean): NetworkCapabilities {
        val capabilities: NetworkCapabilities = mock()
        whenever(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            .thenReturn(internet)
        whenever(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            .thenReturn(validated)
        return capabilities
    }

    @Test
    fun isConnected_noActiveNetwork_returnsFalse() {
        whenever(connectivityManager.activeNetwork).thenReturn(null)

        assertThat(monitor.isConnected(), `is`(false))
    }

    @Test
    fun isConnected_noCapabilities_returnsFalse() {
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(null)

        assertThat(monitor.isConnected(), `is`(false))
    }

    @Test
    fun isConnected_internetNotValidated_returnsFalse() {
        val caps = capabilities(internet = true, validated = false)
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(caps)

        assertThat(monitor.isConnected(), `is`(false))
    }

    @Test
    fun isConnected_validatedWithoutInternet_returnsFalse() {
        val caps = capabilities(internet = false, validated = true)
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(caps)

        assertThat(monitor.isConnected(), `is`(false))
    }

    @Test
    fun isConnected_internetAndValidated_returnsTrue() {
        val caps = capabilities(internet = true, validated = true)
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(caps)

        assertThat(monitor.isConnected(), `is`(true))
    }
}
