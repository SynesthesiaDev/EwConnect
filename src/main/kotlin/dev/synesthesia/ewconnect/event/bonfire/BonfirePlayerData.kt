package dev.synesthesia.ewconnect.event.bonfire

import dev.synesthesia.ewconnect.EwConnect
import io.netty.buffer.Unpooled
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer
import java.util.UUID

data class BonfirePlayerData(
    val uuid: UUID,
    var lastAssignmentTime: Long,
    var lastCompletionTime: Long,
    val flamesCollectedRecently: MutableList<Int>,
    var assignedToday: MutableList<Int>,
    var totalDaysCompleted: Int,
    var bankedEmbers: Int,
) {
    companion object {

        const val ONE_DAY_MS = 24 * 60 * 60 * 1000L
        
        val STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, BonfirePlayerData::uuid,
            ByteBufCodecs.LONG, BonfirePlayerData::lastAssignmentTime,
            ByteBufCodecs.LONG, BonfirePlayerData::lastCompletionTime,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), BonfirePlayerData::flamesCollectedRecently,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), BonfirePlayerData::assignedToday,
            ByteBufCodecs.VAR_INT, BonfirePlayerData::totalDaysCompleted,
            ByteBufCodecs.VAR_INT, BonfirePlayerData::bankedEmbers,
            ::BonfirePlayerData
        )

        val DB_SERIALIZER = object : Serializer<BonfirePlayerData> {

            override fun serialize(data: DataOutput2, bonfirePlayerData: BonfirePlayerData) {
                val buffer = RegistryFriendlyByteBuf(Unpooled.buffer(), EwConnect.server.registryAccess())
                STREAM_CODEC.encode(buffer, bonfirePlayerData)

                val bytes = ByteArray(buffer.readableBytes())
                buffer.readBytes(bytes)

                data.writeInt(bytes.size)
                data.write(bytes)
            }

            override fun deserialize(data: DataInput2, available: Int): BonfirePlayerData? {
                var size = data.readInt()
                var byteArray = ByteArray(size)

                data.readFully(byteArray)
                var buffer = RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(byteArray), EwConnect.server.registryAccess())

                return STREAM_CODEC.decode(buffer)
            }
        }
    }
    
    val isAvailableToday: Boolean get() = (System.currentTimeMillis() - lastCompletionTime) >= ONE_DAY_MS
}