package dev.synesthesia.ewconnect.extensions

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.database.Database
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import java.awt.Color

fun ServerPlayer.send(message: String) = this.sendMessage(ChatUtils.translated(message))

val ServerPlayer.color: String
    get() = Database.getColor(this.uuid) ?: "#ffffff"

val ServerPlayer.nickname: String
    get() = Database.getNickname(this.uuid) ?: this.plainTextName

val ServerPlayer.hasNickname: Boolean
    get() = Database.getNickname(this.uuid) != null

val ServerPlayer.formattedDiscordNickname: String
    get() = buildString {
        if (this@formattedDiscordNickname.hasNickname) {
            append("(${this@formattedDiscordNickname.nickname}) ")
        }
        append(this@formattedDiscordNickname.plainTextName)
    }

val ServerPlayer.formattedChatNickname: String
    get() = buildString {
        if (this@formattedChatNickname.hasNickname) append("<gray>(${this@formattedChatNickname.nickname}) ")
        append(
            "<${this@formattedChatNickname.color}>${this@formattedChatNickname.plainTextName}"
        )
    }


fun ServerPlayer.sendPrivateSound(
    sound: Holder<SoundEvent>,
    x: Double, y: Double, z: Double,
    volume: Float, pitch: Float
) {
    this.connection.send(
        ClientboundSoundPacket(sound, SoundSource.MASTER, x, y, z, volume, pitch, this.random.nextLong())
    )
}

fun ServerPlayer.sendPrivateSound(
    sound: SoundEvent,
    x: Double, y: Double, z: Double,
    volume: Float, pitch: Float
) {
    val soundHolder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound)

    this.connection.send(
        ClientboundSoundPacket(soundHolder, SoundSource.MASTER, x, y, z, volume, pitch, this.random.nextLong())
    )
}

fun ServerPlayer.sendPrivateParticles(
    particle: ParticleOptions,
    x: Double, y: Double, z: Double,
    count: Int, speed: Float
) {
    this.connection.send(
        ClientboundLevelParticlesPacket(
            particle,
            false,
            true,
            x, y, z,
            0.3f, 0.3f, 0.3f,
            speed,
            count
        )
    )
}

fun ServerPlayer.sendScreenTitle(
    titleText: String,
    subtitleText: String,
    fadeIn: Int = 10, stay: Int = 40, fadeOut: Int = 10
) {
    this.connection.send(ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut))
    this.connection.send(ClientboundSetTitleTextPacket(Component.literal(titleText)))
    this.connection.send(ClientboundSetSubtitleTextPacket(Component.literal(subtitleText)))
}

fun ServerPlayer.teleport(blockPos: BlockPos) {
    val x = blockPos.x.toDouble() + 0.5
    val y = blockPos.y.toDouble() + 0.5
    val z = blockPos.z.toDouble() + 0.5
    
    this.teleportTo(x, y, z)
}