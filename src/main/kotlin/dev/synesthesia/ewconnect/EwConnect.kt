package dev.synesthesia.ewconnect

import dev.synesthesia.ewconnect.commands.Commands
import dev.synesthesia.ewconnect.discord.DiscordBot
import me.lucko.spark.api.Spark
import net.fabricmc.api.ModInitializer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.fabric.FabricServerCommandManager
import java.util.UUID

class EwConnect : ModInitializer {

    companion object {
        lateinit var server: MinecraftServer
        lateinit var events: EventHandlers
        lateinit var spark: Spark
        var discordBot: DiscordBot? = null
        val sessionTimes = mutableMapOf<UUID, Long>()
    }

    override fun onInitialize() {
        events = EventHandlers(this)
        val commandManager = FabricServerCommandManager(ExecutionCoordinator.simpleCoordinator(), SenderMapper.identity())

        Commands(commandManager)
        sessionTimes.clear()
    }
}
