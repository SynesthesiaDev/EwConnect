package dev.synesthesia.ewconnect.extensions

import dev.synesthesia.ewconnect.MinecraftChatUtils
import dev.synesthesia.ewconnect.database.Database
import net.minecraft.server.level.ServerPlayer

fun ServerPlayer.send(message: String) = this.sendMessage(MinecraftChatUtils.translated(message))

val ServerPlayer.color: String
    get() = Database.getColor(this.uuid) ?: "#ffffff"

val ServerPlayer.nickname: String
    get() = Database.getNickname(this.uuid) ?: this.plainTextName

val ServerPlayer.hasNickname: Boolean
    get() = Database.getNickname(this.uuid) != null

val ServerPlayer.formattedNickname: String
    get() = buildString {
        if (this@formattedNickname.hasNickname) {
            append("(${this@formattedNickname.nickname}) ")
        }
        append(this@formattedNickname.plainTextName)
    }
