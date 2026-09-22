package dev.synesthesia.ewconnect.extensions

import dev.synesthesia.ewconnect.ChatUtils
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

fun Vec3.toBlockPos(): BlockPos {
    return BlockPos(this.x.toInt(), this.y.toInt(), this.z.toInt())
}

fun String.translated(): Component {
    return ChatUtils.translated(this)
}

fun Vec3.distanceTo(to: Vec3): Double = this.distanceTo(to)