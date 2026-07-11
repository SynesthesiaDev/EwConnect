package dev.synesthesia.ewconnect.utils

import dev.synesthesia.ewconnect.EwConnect
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d

data class Location(val x: Double, val y: Double, val z: Double, val world: String) {

    constructor(vector: Vector3d, world: String) : this(vector.x, vector.y, vector.z, world)
    constructor(blockPos: BlockPos, world: String) : this(blockPos.x, blockPos.y, blockPos.z, world)
    constructor(vector: Vec3, world: String) : this(vector.x, vector.y, vector.z, world)
    constructor(x: Int, y: Int, z: Int, world: String) : this(x.toDouble(), y.toDouble(), z.toDouble(), world)
    constructor(x: Float, y: Float, z: Float, world: String) : this(x.toDouble(), y.toDouble(), z.toDouble(), world)
    constructor(x: Long, y: Long, z: Long, world: String) : this(x.toDouble(), y.toDouble(), z.toDouble(), world)

    fun toBlockPos(): BlockPos = BlockPos(x.toInt(), y.toInt(), z.toInt())
    fun toVec3(): Vec3 = Vec3(x, y, z)
    fun toVec3i(): Vec3i = Vec3i(x.toInt(), y.toInt(), z.toInt())

    fun getServerLevelOrNull(): ServerLevel? {
        val resourceLocation = Identifier.tryParse(this.world) ?: return null
        val levelKey = ResourceKey.create(Registries.DIMENSION, resourceLocation)
        return EwConnect.server.getLevel(levelKey)
    }

    fun getServerLevel(): ServerLevel {
        return getServerLevelOrNull() ?: throw Exception("World with identifier `${world}` was not found")
    }

    fun subtract(x: Double, y: Double, z: Double): Location {
        return Location(this.x - x, this.y - y, this.z - z, world)
    }

    fun add(x: Double, y: Double, z: Double): Location {
        return Location(this.x + x, this.y + y, this.z + z, world)
    }

    companion object {

        val STEAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Location::x,
            ByteBufCodecs.DOUBLE, Location::y,
            ByteBufCodecs.DOUBLE, Location::z,
            ByteBufCodecs.STRING_UTF8, Location::world,
            ::Location
        )

        fun fromPlayer(player: ServerPlayer): Location {
            return Location(player.position(), player.level().dimension().identifier().toShortString())
        }

        fun fromEntity(entity: Entity): Location {
            return Location(entity.position(), entity.level().dimension().identifier().toShortString())
        }
    }
}