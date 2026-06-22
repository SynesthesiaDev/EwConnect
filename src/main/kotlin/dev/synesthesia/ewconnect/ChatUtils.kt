package dev.synesthesia.ewconnect

import dev.synesthesia.ewconnect.discord.DiscordBot
import dev.synesthesia.ewconnect.extensions.color
import dev.synesthesia.ewconnect.extensions.nickname
import dev.synesthesia.ewconnect.extensions.hasNickname
import dev.synesthesia.ewconnect.extensions.tintWhite
import dev.synesthesia.ewconnect.extensions.hex
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.server.level.ServerPlayer
import java.awt.Color

object ChatUtils {

    private const val DISCORD_PREFIX = "<#7289da>(DC) "
    private var minimessage = MiniMessage.miniMessage()

    fun translated(message: String): Component {
        return minimessage.deserialize(message)
    }

    fun sendPlayerChatMessage(player: ServerPlayer, message: String) {
        val string = buildString {
            if (player.hasNickname) append("<gray>(${player.nickname}) ")
            append(
                "<${player.color}>${player.plainTextName}: <${
                    Color.decode(player.color).tintWhite(0.8f).hex
                }>${message}"
            )
        }
        sendMessage(string)
    }

    fun sendMessage(message: String, vararg player: ServerPlayer) {
        val component = minimessage.deserialize(message)
        EwConnect.server.playerList.players.filter { p -> !player.contains(p) }.forEach { player -> player.sendMessage(component) }
        EwConnect.server.sendMessage(component)
    }

    fun sendFromDiscord(name: String, color: Color, message: String, reply: DiscordBot.Reply?, hasAttachments: Boolean) {
        val mediaAttached = if(hasAttachments) "<gray>(+ media attached)" else "" 
        if (reply != null) {
            val cutMessage = if(reply.message.length > 35) "${reply.message.substring(0, 35)}..." else reply.message
            sendMessage("${DISCORD_PREFIX}| <italic><gray>${reply.author}: <#d1d1d1>${cutMessage}</italic>")
            sendMessage("      <#7289da>→ <${color.hex}>${name}: <white>${message} $mediaAttached")
        } else {
            sendMessage("${DISCORD_PREFIX}<${color.hex}>${name}: <white>${message} $mediaAttached")
        }
    }

    fun sendBotMessage(message: String) = sendMessage("${DISCORD_PREFIX}<white>${message}")

}