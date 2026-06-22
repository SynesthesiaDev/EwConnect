package dev.synesthesia.ewconnect.event.treasurehunt

import dev.synesthesia.ewconnect.EwConnect
import io.netty.buffer.Unpooled
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer
import java.util.Optional
import java.util.UUID

data class TreasureHuntData(
    val uuid: UUID,
    var started: Boolean,
    var finished: Boolean,
    var nextHint: Optional<TreasureHuntEvent.Hint>,
    val collectedHints: MutableList<TreasureHuntEvent.Hint>
) {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, TreasureHuntData::uuid,
            ByteBufCodecs.BOOL, TreasureHuntData::started,
            ByteBufCodecs.BOOL, TreasureHuntData::finished,
            ByteBufCodecs.optional(TreasureHuntEvent.Hint.STREAM_CODEC), TreasureHuntData::nextHint,
            TreasureHuntEvent.Hint.STREAM_CODEC.apply(ByteBufCodecs.list()), TreasureHuntData::collectedHints,
            ::TreasureHuntData
        )

        val DB_SERIALIZER = object : Serializer<TreasureHuntData> {

            override fun serialize(data: DataOutput2, treasureHuntData: TreasureHuntData) {
                val buffer = RegistryFriendlyByteBuf(Unpooled.buffer(), EwConnect.server.registryAccess())
                STREAM_CODEC.encode(buffer, treasureHuntData)

                val bytes = ByteArray(buffer.readableBytes())
                buffer.readBytes(bytes)

                data.writeInt(bytes.size)
                data.write(bytes)
            }

            override fun deserialize(data: DataInput2, available: Int): TreasureHuntData? {
                var size = data.readInt()
                var byteArray = ByteArray(size)

                data.readFully(byteArray)
                var buffer =
                    RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(byteArray), EwConnect.server.registryAccess())

                return STREAM_CODEC.decode(buffer)
            }
        }
    }
}