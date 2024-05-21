package com.gago.weatherapp.data.datastore

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings
        get() = Settings()

    override suspend fun readFrom(input: InputStream): Settings {
        return try {
            Json.decodeFromString(
                deserializer = Settings.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = Settings.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }

}