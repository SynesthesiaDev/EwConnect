package dev.synesthesia.ewconnect.settings

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class DiscordSettings(
    val token: String,
    val clientId: Long,
    val guildId: Long,
    val channelId: Long
) {
    companion object {
        val recordCodec: Codec<DiscordSettings?> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("token").forGetter(DiscordSettings::token),
                Codec.LONG.fieldOf("client_id").forGetter(DiscordSettings::clientId),
                Codec.LONG.fieldOf("guild_id").forGetter(DiscordSettings::guildId),
                Codec.LONG.fieldOf("channel_id").forGetter(DiscordSettings::channelId),
            ).apply(instance, ::DiscordSettings)
        }
    }
}