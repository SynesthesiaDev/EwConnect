package dev.synesthesia.ewconnect.entities

import com.mojang.authlib.GameProfile
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.mixin.PlayerListAccessor
import net.minecraft.stats.ServerStatsCounter
import java.util.UUID

data class OfflinePlayer(
    val profile: GameProfile,
    val statistics: ServerStatsCounter,
) {

    val username: String get() = profile.name
    val uuid: UUID get() = profile.id
    
    companion object {
        fun fromGameProfile(gameProfile: GameProfile): OfflinePlayer {
            val server = EwConnect.server

            val onlinePlayer = server.playerList.getPlayer(gameProfile.id)
            if (onlinePlayer != null) {
                val stats = onlinePlayer.stats
                return OfflinePlayer(gameProfile, stats)
            }

            val playerListAccessor = server.playerList as PlayerListAccessor

            val statsCounter = playerListAccessor.statsMap.computeIfAbsent(gameProfile.id, { id ->
                val file = playerListAccessor.getLocateStatsFile(gameProfile)
                return@computeIfAbsent ServerStatsCounter(EwConnect.server, file)
            })

            return OfflinePlayer(gameProfile, statsCounter)
        }
    }
}