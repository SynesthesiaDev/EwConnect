package dev.synesthesia.ewconnect.utils

import net.kyori.adventure.bossbar.BossBar
import net.minecraft.server.level.ServerPlayer
import org.geysermc.floodgate.api.FloodgateApi

object BossbarUtil {

    @Suppress("SENSELESS_COMPARISON")
    fun show(player: ServerPlayer, bossbar: BossBar) {
        if (FloodgateApi.getInstance().isFloodgatePlayer(player.uuid)) return

        bossbar.addViewer(player)

        val ticks = 10 * 20
        FabricScheduler.repeatWithDelay(times = ticks, delayTicks = 1) { index ->
            if (!player.isAlive || player.connection == null) return@repeatWithDelay

            val ticksRemaining = ticks - (index + 1)
            val progress = ticksRemaining.toFloat() / ticks.toFloat()

            bossbar.progress(progress.coerceIn(0f, 1f))

            if (ticksRemaining <= 0) {
                bossbar.removeViewer(player)
            }
        }
    }
}