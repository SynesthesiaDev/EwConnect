package dev.synesthesia.ewconnect

import dev.synesthesia.ewconnect.discord.DiscordBot
import dev.synesthesia.ewconnect.extensions.ToNMSComponent
import dev.synesthesia.ewconnect.extensions.formattedNickname
import dev.synesthesia.ewconnect.settings.Settings
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.network.Connection
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket
import net.minecraft.network.protocol.status.ServerStatus
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats

class EventHandlers(val mod: EwConnect) {

    init {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            EwConnect.server = server
            Settings.load()

            if (Settings.current.discord != null && Settings.current.discord!!.clientId != 0L) {
                EwConnect.discordBot = DiscordBot(Settings.current.discord!!)
            }
        }

        ServerPlayerEvents.JOIN.register { player ->
            EwConnect.discordBot?.onPlayerJoin(
                player.formattedNickname,
                player.uuid,
                player.stats.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
            )
        }

        ServerPlayerEvents.LEAVE.register { player ->
            EwConnect.discordBot?.onPlayerLeave(
                player.formattedNickname,
                player.uuid,
                player.stats.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
            )
        }


        ServerLivingEntityEvents.AFTER_DEATH.register { entity, source ->
            if (entity !is ServerPlayer) return@register
            val message = source.getLocalizedDeathMessage(entity).string
            EwConnect.discordBot?.onPlayerDeath(
                message,
                entity.uuid,
                entity.stats.getValue(Stats.CUSTOM.get(Stats.DEATHS))
            )
        }
    }

    companion object {

        private const val MOTD: String = "<#fcdbff>the <#f475ff>ew nation <#fcdbff>Minecraft survival server!"
        private val TRANSLATED_MOTD: Component = MinecraftChatUtils.translated(MOTD).ToNMSComponent()

        @JvmStatic
        fun onChatMessageCallback(player: ServerPlayer, message: String) {
            EwConnect.discordBot?.onPlayerChat(player.formattedNickname, message)
            MinecraftChatUtils.sendPlayerChatMessage(player, message)
        }

        @JvmStatic
        fun onServerStatusPingCallback(connection: Connection, oldStatus: ServerStatus) {
            val newStatus = ServerStatus(
                TRANSLATED_MOTD,
                oldStatus.players,
                oldStatus.version,
                oldStatus.favicon,
                oldStatus.enforcesSecureChat()
            )
            connection.send(ClientboundStatusResponsePacket(newStatus))
        }
    }

}