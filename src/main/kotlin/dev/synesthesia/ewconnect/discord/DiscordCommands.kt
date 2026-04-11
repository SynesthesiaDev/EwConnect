package dev.synesthesia.ewconnect.discord

import dev.minn.jda.ktx.events.onCommand
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.extensions.formattedNickname

class DiscordCommands(val bot: DiscordBot) {
    init {
        val jda = bot.jda

        bot.guild?.upsertCommand("list", "list users on the ew smp")?.queue()

        jda.onCommand("list") { event ->
            if (event.guild?.idLong != bot.settings.guildId) return@onCommand

            val players = EwConnect.server.playerList.players
            val response = buildString {
                appendLine("**Players Online (${players.size}):**")
                if (players.isEmpty()) appendLine("_No one is online._")
                else players.forEach { appendLine("> ${it.formattedNickname}") }
            }

            event.reply(response).queue()
        }
    }
}