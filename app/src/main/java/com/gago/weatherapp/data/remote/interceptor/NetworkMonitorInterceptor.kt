package com.gago.weatherapp.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import kotlin.jvm.Throws

class NetworkMonitorInterceptor @Inject constructor(private val networkMonitor: NetworkMonitor) :
    Interceptor {

    @Throws(NoNetworkException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (networkMonitor.isConnected()) {
            return chain.proceed(request)
        } else {
            throw NoNetworkException("Network not available")
        }
    }
}