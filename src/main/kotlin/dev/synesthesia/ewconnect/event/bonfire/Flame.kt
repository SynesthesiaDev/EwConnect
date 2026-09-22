package dev.synesthesia.ewconnect.event.bonfire

import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

data class Flame(val position: BlockPos) {
    companion object {
        fun fromId(id: Int): Flame = BonfireFestivalEvent.FLAMES[id]

        val STREAM_CODEC: StreamCodec<ByteBuf, Flame> = ByteBufCodecs.VAR_INT.map(
            { id -> fromId(id) },
            { hint -> hint.id() }
        )
    }
    
    fun id(): Int = BonfireFestivalEvent.FLAMES.indexOf(this)
}