package dev.synesthesia.ewconnect.extensions

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.ComponentSerialization

// ugly ass hack but whatever not like there's better way to do it currently
fun Component.ToNMSComponent(): net.minecraft.network.chat.Component {
    val json = GsonComponentSerializer.gson().serialize(this)
    return ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow { e -> throw Exception(e) }.first
}