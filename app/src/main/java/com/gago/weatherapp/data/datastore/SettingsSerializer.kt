package com.gago.weatherapp.data.datastore

import androidx.datastore.core.Serializer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
            FirebaseCrashlytics.getInstance().recordException(e)
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

class MyPersistentListSerializer(
    private val elementSerializer: KSerializer<WeatherLocal>
) : KSerializer<PersistentList<WeatherLocal>> {

    @OptIn(SealedSerializationApi::class)
    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<WeatherLocal>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()
    override fun deserialize(decoder: Decoder): PersistentList<WeatherLocal> {
        return ListSerializer(elementSerializer).deserialize(decoder).toPersistentList()
    }

    override fun serialize(encoder: Encoder, value: PersistentList<WeatherLocal>) {
        return ListSerializer(elementSerializer).serialize(encoder, value)
    }

}