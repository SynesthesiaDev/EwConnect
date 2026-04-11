package dev.synesthesia.ewconnect.settings

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import java.io.File

object Settings {

    private const val FILE_PATH = "./ewconnect.json"

    var current: SettingsData = SettingsData.default;

    fun load() {
        val file = File(FILE_PATH)

        if (!file.exists()) {
            file.createNewFile()

            val encoded = SettingsData.recordCodec.encodeStart(JsonOps.INSTANCE, SettingsData.default)
                .getOrThrow { error -> throw RuntimeException(error) }

            file.writeText(encoded.toString())

        } else {
            val text = file.readText()
            val decoded = SettingsData.recordCodec.parse(JsonOps.INSTANCE, JsonParser.parseString(text))
                .getOrThrow { error -> throw RuntimeException(error) }

            current = decoded;
        }
    }
}