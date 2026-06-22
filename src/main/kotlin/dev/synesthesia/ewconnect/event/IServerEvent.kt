package dev.synesthesia.ewconnect.event

import net.kyori.adventure.bossbar.BossBar
import net.minecraft.server.level.ServerPlayer

interface IServerEvent {

    val title: String
    val overridesMotd: String? get() = null

    fun onPlayerJoin(player: ServerPlayer)

    fun onPlayerLeave(player: ServerPlayer)

}