package dev.synesthesia.ewconnect.extensions

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.dv8tion.jda.api.components.tree.ComponentTree
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.ComponentSerialization

// ugly ass hack but whatever not like there's better way to do it currently
fun Component.toNMSComponent(): net.minecraft.network.chat.Component {
    
    val rootComponent = Component.text().decoration(TextDecoration.ITALIC, false).build().append(this)
    val json = GsonComponentSerializer.gson().serialize(rootComponent)
    return ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow { e -> throw Exception(e) }.first
}