package com.gago.weatherapp.data.datastore

import androidx.datastore.core.Serializer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object WeatherCacheSerializer : Serializer<WeatherCache> {

    /** Tolerates entries written by older/newer versions of the models. */
    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue: WeatherCache
        get() = WeatherCache()

    override suspend fun readFrom(input: InputStream): WeatherCache {
        return try {
            json.decodeFromString(
                deserializer = WeatherCache.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            defaultValue
        }
    }

    override suspend fun writeTo(t: WeatherCache, output: OutputStream) {
        output.write(
            json.encodeToString(
                serializer = WeatherCache.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}
