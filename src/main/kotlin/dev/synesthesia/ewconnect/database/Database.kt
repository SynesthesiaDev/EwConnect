package dev.synesthesia.ewconnect.database

import dev.synesthesia.ewconnect.database.serializers.ListDatabaseSerializer
import dev.synesthesia.ewconnect.event.treasurehunt.TreasureHuntData
import net.minecraft.core.BlockPos
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import java.util.Optional
import java.util.UUID

object Database {
    private var database: DB = DBMaker
        .fileDB("./data.db")
        .transactionEnable()
        .closeOnJvmShutdown()
        .make()

    private var nicknames = database.hashMap("nicknames", Serializer.UUID, Serializer.STRING).createOrOpen()
    private var colors = database.hashMap("colors", Serializer.UUID, Serializer.STRING).createOrOpen()

    private var graves = database.hashMap(
        name = "graves",
        keySerializer = Serializer.UUID,
        valueSerializer = ListDatabaseSerializer(PlayerGrave.DB_SERIALIZER)
    ).createOrOpen()

    private val treasureHuntData = database.hashMap(
        name = "treasure_hunt_data",
        keySerializer = Serializer.UUID,
        valueSerializer = TreasureHuntData.DB_SERIALIZER
    ).createOrOpen()

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

    fun getGraves(uuid: UUID) = graves[uuid] ?: emptyList()

    fun addGrave(grave: PlayerGrave) {
        val graveList = graves[grave.uuid]?.toMutableList() ?: mutableListOf()
        graveList.add(grave)

        graves[grave.uuid] = graveList
        database.commit()
    }

    fun getGraveAt(uuid: UUID, blockPos: BlockPos): PlayerGrave? =
        getGraves(uuid).firstOrNull { p -> p.location == blockPos }

    fun getAllGraves(): HTreeMap<UUID, List<PlayerGrave>> = graves

    fun removeGrave(grave: PlayerGrave) {
        val graveList = graves[grave.uuid]?.toMutableList() ?: return
        graveList.remove(grave)

        graves[grave.uuid] = graveList
        database.commit()
    }

    fun getTreasureHuntData(uuid: UUID): TreasureHuntData {
        var data = treasureHuntData[uuid]
        if (data == null) {
            data = TreasureHuntData(
                uuid = uuid,
                started = false,
                finished = false,
                nextHint = Optional.empty(),
                collectedHints = mutableListOf()
            )
        }

        treasureHuntData[uuid] = data
        database.commit()

        return data
    }

    fun editTreasureHuntData(uuid: UUID, unit: (TreasureHuntData) -> Unit) {
        val data = getTreasureHuntData(uuid)

        unit.invoke(data)

        treasureHuntData[uuid] = data
        database.commit()
    }

}