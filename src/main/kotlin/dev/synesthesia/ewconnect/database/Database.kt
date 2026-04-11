package dev.synesthesia.ewconnect.database

import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.util.UUID

object Database {
    private var database: DB = DBMaker
        .fileDB("./data.db")
        .transactionEnable()
        .closeOnJvmShutdown()
        .make()
    
    private var nicknames = database.hashMap("nicknames", Serializer.UUID, Serializer.STRING).createOrOpen()
    private var colors = database.hashMap("colors", Serializer.UUID, Serializer.STRING).createOrOpen()

    fun setNickname(uuid: UUID, nickname: String?) {
        if (nickname == null) nicknames.remove(uuid) else nicknames[uuid] = nickname
        database.commit()
    }
    
    fun getNickname(uuid: UUID): String? = nicknames[uuid]

    fun setColor(uuid: UUID, color: String?) {
        if (color == null) colors.remove(uuid) else colors[uuid] = color.trim().lowercase()
        database.commit()
    }

    fun getColor(uuid: UUID): String? = colors[uuid]
}