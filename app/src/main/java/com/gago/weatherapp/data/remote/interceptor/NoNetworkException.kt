package com.gago.weatherapp.data.remote.interceptor

import java.io.IOException

class NoNetworkException(message: String) : IOException(message)