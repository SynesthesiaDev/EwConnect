package dev.synesthesia.ewconnect.settings

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class SettingsData(
    val schemaVersion: Int,
    val discord: DiscordSettings?
) {
    companion object {

        private const val CURRENT_SCHEMA_VERSION = 0

        val default = SettingsData(CURRENT_SCHEMA_VERSION, DiscordSettings("token123", 0, 0, 0))

        val recordCodec = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("_configVersion").forGetter(SettingsData::schemaVersion),
                DiscordSettings.recordCodec.fieldOf("discord").forGetter(SettingsData::discord)

            ).apply(instance, ::SettingsData)
        }
    }
}