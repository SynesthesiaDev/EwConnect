package dev.synesthesia.ewconnect.discord

import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.messages.Embed
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.MinecraftChatUtils
import dev.synesthesia.ewconnect.msToReadable
import dev.synesthesia.ewconnect.settings.DiscordSettings
import dev.synesthesia.ewconnect.settings.Settings
import dev.synesthesia.ewconnect.ticksToReadable
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.minecraft.core.BlockPos
import net.minecraft.server.TickTask
import java.awt.Color
import java.util.UUID

class DiscordBot(val settings: DiscordSettings) {

    private val embedColorJoin = 0x97ff6b
    private val embedColorLeave = 0xff6b72
    private val embedColorDeath = 0x66000a
    private val embedColorReclaim = 0x34ebe1
    private val embedColorAdvancement = 0xffef08
    private val avatarUrlBase = "https://mc-heads.net/head/"

    val jda = light(settings.token) {
        setEventManager(CoroutineEventManager())
        enableIntents(
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
        )
    }

    var channel: TextChannel? = null
    var guild: Guild? = null

    init {
        jda.listener<ReadyEvent> { _ ->

            guild = jda.getGuildById(Settings.current.discord!!.guildId)
                ?: throw RuntimeException("Failed to get guild!")

            channel = jda.getTextChannelById(Settings.current.discord!!.channelId)
                ?: throw RuntimeException("Failed to get channel!")

            MinecraftChatUtils.sendBotMessage("<#95ff7a>Discord bot has loaded!")
            DiscordCommands(this@DiscordBot)
            updatePlayerCount()
        }

        jda.listener<MessageReceivedEvent> { event ->
            if (event.author.isBot) return@listener
            if (event.guild.idLong != settings.guildId) return@listener
            if (event.channel.idLong != settings.channelId) return@listener
            var member = event.message.member!!

            var color = member.colors.primary ?: Color(227, 245, 255)
            MinecraftChatUtils.sendFromDiscord(
                member.nickname ?: member.user.effectiveName,
                color,
                event.message.contentRaw
            )
        }
    }

    fun onPlayerChat(name: String, message: String) {
        channel?.sendMessage("**${name}:** $message")?.queue()
    }

    fun onPlayerJoin(name: String, uuid: UUID, playtimeTicks: Int) {
        val embed = Embed {
            title = "$name has joined the server!"
            description = getPlaytime(playtimeTicks)
            color = embedColorJoin
            thumbnail = getAvatarUrl(uuid)
        }
        channel?.sendMessageEmbeds(embed)?.queue()
        updatePlayerCount()
    }

    fun onPlayerLeave(name: String, uuid: UUID, sessionPlaytime: Long, playtimeTicks: Int) {
        val embed = Embed {
            title = "$name has left the server!"
            description = "They played for `${(System.currentTimeMillis() - sessionPlaytime).msToReadable()}` this session\nTheir total playtime is `${playtimeTicks.ticksToReadable()}`"
            color = embedColorLeave
            thumbnail = getAvatarUrl(uuid)
        }
        channel?.sendMessageEmbeds(embed)?.queue()

        updatePlayerCount()
    }

    fun onPlayerDeath(deathMessage: String, uuid: UUID, deaths: Int, graveLocation: String) {
        val embed = Embed {
            title = deathMessage
            description = "\uD83D\uDC80 They have now died $deaths time(s)\n\n\uD83E\uDEA6 Their grave is at $graveLocation"
            color = embedColorDeath
            thumbnail = getAvatarUrl(uuid)
        }
        channel?.sendMessageEmbeds(embed)?.queue()
    }
    
    fun onGraveReclaim(name: String, uuid: UUID) {
        val embed = Embed {
            title = "$name has reclaimed their grave!"
            color = embedColorReclaim
            thumbnail = getAvatarUrl(uuid)
        }
        channel?.sendMessageEmbeds(embed)?.queue()
    }

    fun onAdvancement(name: String, uuid: UUID, advancement: String, advancementDescription: String) {
        val embed = Embed {
            title = "$name has made an advancement"
            description = "\n**${advancement}**\n${advancementDescription}\n"
            color = embedColorAdvancement
            thumbnail = getAvatarUrl(uuid)
        }
        channel?.sendMessageEmbeds(embed)?.queue()
    }

    private fun getAvatarUrl(uuid: UUID): String = "${avatarUrlBase}/$uuid/100"
    
    private fun getPlaytime(ticks: Int): String = "Their playtime is: `${ticks.ticksToReadable()}`"

    private fun updatePlayerCount() {
        // Schedule on main server thread to prevent race condition on the coroutine
        EwConnect.server.schedule(TickTask(1, {
            val playerCount = EwConnect.server.playerList.players.size
            jda.presence.setPresence(OnlineStatus.ONLINE, Activity.playing("$playerCount players on ew smp"))
        }))
    }
}