package dev.synesthesia.ewconnect.extensions

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

fun Vec3.toBlockPos(): BlockPos {
    return BlockPos(this.x.toInt(), this.y.toInt(), this.z.toInt())
}