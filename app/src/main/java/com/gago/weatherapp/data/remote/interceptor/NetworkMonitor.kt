package com.gago.weatherapp.data.remote.interceptor

interface NetworkMonitor {
    fun isConnected(): Boolean
}