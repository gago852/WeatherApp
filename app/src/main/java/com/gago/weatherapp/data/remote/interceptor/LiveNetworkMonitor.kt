package com.gago.weatherapp.data.remote.interceptor

import android.content.Context
import android.net.ConnectivityManager
import javax.inject.Inject

class LiveNetworkMonitor @Inject constructor(private val context: Context) : NetworkMonitor {
    override fun isConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetwork
        return networkInfo != null
    }

}