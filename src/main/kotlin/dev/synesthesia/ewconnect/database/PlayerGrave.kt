package dev.synesthesia.ewconnect.database

import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.database.serializers.ItemStackListDbSerializer
import io.netty.buffer.Unpooled
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer
import java.util.UUID

data class PlayerGrave(
    val uuid: UUID,
    val lastKnowUsername: String,
    val location: BlockPos,
    val world: String,
    val items: List<ItemStack>
) {

    companion object {

        val STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PlayerGrave::uuid,
            ByteBufCodecs.STRING_UTF8, PlayerGrave::lastKnowUsername,
            BlockPos.STREAM_CODEC, PlayerGrave::location,
            ByteBufCodecs.STRING_UTF8, PlayerGrave::world,
            ItemStackListDbSerializer.ITEM_STACK_LIST_SERIALIZER, PlayerGrave::items,
            ::PlayerGrave
        )

        val DB_SERIALIZER = object : Serializer<PlayerGrave> {

            override fun serialize(data: DataOutput2, grave: PlayerGrave) {
                val buffer = RegistryFriendlyByteBuf(Unpooled.buffer(), EwConnect.server.registryAccess())
                STREAM_CODEC.encode(buffer, grave)

                val bytes = ByteArray(buffer.readableBytes())
                buffer.readBytes(bytes)

                data.writeInt(bytes.size)
                data.write(bytes)
            }

            override fun deserialize(data: DataInput2, available: Int): PlayerGrave? {
                var size = data.readInt()
                var byteArray = ByteArray(size)

                data.readFully(byteArray)
                var buffer =
                    RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(byteArray), EwConnect.server.registryAccess())

                return STREAM_CODEC.decode(buffer)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerGrave) return false

        return location == other.location && world == other.world
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + world.hashCode()
        return result
    }
}