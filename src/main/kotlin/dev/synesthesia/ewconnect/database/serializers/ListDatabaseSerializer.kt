package dev.synesthesia.ewconnect.database.serializers

import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer


class ListDatabaseSerializer<T>(private val inner: Serializer<T>) : Serializer<List<T>> {

    override fun serialize(data: DataOutput2, list: List<T>) {
        data.writeInt(list.size)
        list.forEach { item ->
            if (item != null) inner.serialize(data, item)
        }
    }

    override fun deserialize(data: DataInput2, available: Int): List<T> {
        val size = data.readInt()
        val list = ArrayList<T>(size)

        repeat(size) {
            val item = inner.deserialize(data, available)
            if (item != null) list.add(item)
        }

        return list
    }

}