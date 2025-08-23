package com.gago.weatherapp.domain.utils

sealed interface DataError: Error {
    enum class Network: DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        UNKNOWN
    }
    
    enum class Local: DataError {
        DISK_FULL,
        UNKNOWN
    }
    
    enum class Places: DataError {
        QUOTA_EXCEEDED,
        OVER_QUERY_LIMIT,
        INVALID_REQUEST,
        NOT_FOUND,
        UNKNOWN
    }
    
    enum class Weather: DataError {
        API_KEY_INVALID,
        API_KEY_EXPIRED,
        QUOTA_EXCEEDED,
        CITY_NOT_FOUND,
        INVALID_COORDINATES,
        UNKNOWN
    }
}