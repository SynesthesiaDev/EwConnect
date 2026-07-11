package dev.synesthesia.ewconnect.entities

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.extensions.toNMSComponent
import dev.synesthesia.ewconnect.utils.Location
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.phys.Vec3

class HologramEntity(var location: Location, val lines: MutableList<IHologramLine>, val updateSeconds: Int? = null) :
    IFakeEntity {

    companion object {
        private const val LINE_SPACING: Double = 0.25
    }

    private val line2Entity: MutableMap<IHologramLine, Entity> = mutableMapOf()

    fun isActiveArmorStand(armorStand: ArmorStand): Boolean = line2Entity.values.any { it.id == armorStand.id }

    fun create() {
        val startingLocation = location.toVec3()
        var offset = 0.0;

        lines.reversed().forEach { line ->
            val offsetLocation = startingLocation.add(0.0, offset, 0.0)
            val entity = spawnArmorStand(offsetLocation, line.getLineText())
            offset += LINE_SPACING

            line2Entity[line] = entity

            location.getServerLevel().addFreshEntity(entity)
        }

        val chunkPos = location.getServerLevel().getChunk(location.toBlockPos()).pos
        location.getServerLevel().setChunkForced(chunkPos.x, chunkPos.z, true)
    }

    fun update() {
        line2Entity.forEach { (line, entity) ->
            entity.customName = ChatUtils.translated(line.getLineText()).toNMSComponent()
        }
    }

    private fun spawnArmorStand(spawnLocation: Vec3, text: String): ArmorStand {
        val level = location.getServerLevel()
        val stand = ArmorStand(level, spawnLocation.x, spawnLocation.y, spawnLocation.z)

        stand.isInvisible = true
        stand.customName = ChatUtils.translated(text).toNMSComponent()
        stand.isCustomNameVisible = true
        stand.isNoGravity = true
        stand.isInvisible = true

        stand.addTag("ewconnect_cleanup_orphan")

        return stand
    }

    override fun dispose() {
        line2Entity.forEach { (_, entity) ->
            entity.discard()
        }
    }

    class Builder {
        private val lines: MutableList<IHologramLine> = mutableListOf()
        private var location: Location? = null

        private var updateRate: Int? = null

        fun setUpdateRate(seconds: Int) {
            updateRate = seconds
        }

        fun setLocation(location: Location) {
            this.location = location
        }

        fun addStatic(text: String) {
            lines.add(StaticHologramLine(text))
        }

        fun addDynamic(dynamic: () -> String) {
            lines.add(DynamicHologramLine(dynamic))
        }

        fun addOfflinePlayer(player: OfflinePlayer, dynamic: (OfflinePlayer) -> String) {
            lines.add(OfflinePlayerHologramLine(player, dynamic))
        }

        fun build(): HologramEntity {
            if (location == null) throw Exception("Location cannot be null")
            val entity = HologramEntity(location!!, lines, updateRate)

            return entity
        }
    }

    interface IHologramLine {
        fun getLineText(): String
    }

    data class StaticHologramLine(val text: String) : IHologramLine {
        override fun getLineText(): String = text
    }

    data class DynamicHologramLine(val dynamic: () -> String) : IHologramLine {
        override fun getLineText(): String = dynamic.invoke()
    }

    data class OfflinePlayerHologramLine(val player: OfflinePlayer, val dynamic: (player: OfflinePlayer) -> String) :
        IHologramLine {
        override fun getLineText(): String = dynamic.invoke(player)
    }
}