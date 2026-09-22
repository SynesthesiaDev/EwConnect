package dev.synesthesia.ewconnect.event

import net.kyori.adventure.bossbar.BossBar
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.incendo.cloud.CommandManager
import org.incendo.cloud.fabric.FabricServerCommandManager

interface IServerEvent {

    val title: String
    val overridesMotd: String? get() = null

    fun onInit(server: MinecraftServer) {}
    
    fun onPlayerJoin(player: ServerPlayer) {}

    fun onPlayerLeave(player: ServerPlayer) { }
    
    fun registerCommands(commandManager: FabricServerCommandManager<CommandSourceStack>) {}

}