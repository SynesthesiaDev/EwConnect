package dev.synesthesia.ewconnect.graveyard

import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.entities.OfflinePlayer
import dev.synesthesia.ewconnect.extensions.color
import dev.synesthesia.ewconnect.ticksToReadable
import dev.synesthesia.ewconnect.utils.Location
import net.minecraft.stats.Stats
import java.util.*

object GraveyardManager {

    fun createOrUpdate(username: String, location: Location, epitaph: String) {
        val uuid = EwConnect.getEveryPlayerEver().map { profile -> OfflinePlayer.fromGameProfile(profile) }
            .firstOrNull { p -> p.username == username }
            ?: throw Exception("Failed to get uuid from username of $username")

        createOrUpdate(uuid.uuid, location, epitaph)
    }

    fun createOrUpdate(uuid: UUID, location: Location, epitaph: String) {
        val graveyardInfo = GraveyardInfo(uuid, location, epitaph)
        Database.graveyardData[uuid] = graveyardInfo

        createHolograms()
        Database.database.commit()
    }

    fun createHolograms() {
        val hologramManager = EwConnect.hologramManager
        val allPlayers = EwConnect.getEveryPlayerEver().map { profile -> OfflinePlayer.fromGameProfile(profile) }

        Database.graveyardData.forEach { (uuid, info) ->
            val id = "graveyard_grave_${uuid}"
            val player = allPlayers.firstOrNull { player -> player.uuid == uuid }

            hologramManager.remove(id)

            hologramManager.create(id) {
                setLocation(info.location)
                setUpdateRate(5)

                if (player == null) {
                    addStatic("<red><bold>${info.uuid}")
                    addStatic("<red>Failed to load graveyard data")
                } else {
                    addDynamic { "<${player.color}><bold>${player.username}" }
                    addDynamic { "<white>Deaths: <red>${player.statistics.getValue(Stats.CUSTOM.get(Stats.DEATHS))} ☠" }
                    addDynamic { "<white>Damage Taken: <gold>${player.statistics.getValue(Stats.CUSTOM.get(Stats.DAMAGE_TAKEN))} \uD83D\uDDE1" }
                    addDynamic { "<white>Last death: <yellow>${player.statistics.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH)).ticksToReadable()} ⌛" }
                    addStatic("")
                    addStatic("<${player.color}><italic>${info.epitaph}")
                }
            }
        }
    }
}