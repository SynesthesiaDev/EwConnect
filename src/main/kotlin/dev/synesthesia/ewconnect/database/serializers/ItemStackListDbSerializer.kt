package dev.synesthesia.ewconnect.database.serializers

import io.netty.buffer.Unpooled
import net.minecraft.core.RegistryAccess
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.item.ItemStack
import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer

class ItemStackListDbSerializer(private val registries: RegistryAccess) : Serializer<List<ItemStack>> {

    companion object {
        public val ITEM_STACK_LIST_SERIALIZER = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list())
    }

    override fun serialize(data: DataOutput2, itemStacks: List<ItemStack>) {
        val buffer = RegistryFriendlyByteBuf(Unpooled.buffer(), registries)
        ITEM_STACK_LIST_SERIALIZER.encode(buffer, itemStacks)

        val array = buffer.array()
        data.write(array.size)
        data.write(array)
    }

    override fun deserialize(data: DataInput2, size: Int): List<ItemStack>? {
        val byteSize = data.readInt()
        val bytes = ByteArray(byteSize)

        data.readFully(bytes)
        val buffer = RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), registries)

        return ITEM_STACK_LIST_SERIALIZER.decode(buffer)
    }
}