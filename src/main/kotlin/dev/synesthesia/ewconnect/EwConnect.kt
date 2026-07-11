package dev.synesthesia.ewconnect

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import dev.synesthesia.ewconnect.commands.Commands
import dev.synesthesia.ewconnect.commands.GraveyardCommands
import dev.synesthesia.ewconnect.commands.TreasureHuntCommands
import dev.synesthesia.ewconnect.discord.DiscordBot
import dev.synesthesia.ewconnect.entities.HologramManager
import dev.synesthesia.ewconnect.event.IServerEvent
import dev.synesthesia.ewconnect.event.treasurehunt.TreasureHuntEvent
import me.lucko.spark.api.Spark
import net.fabricmc.api.ModInitializer
import net.minecraft.server.MinecraftServer
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.fabric.FabricServerCommandManager
import java.nio.file.Files
import java.util.UUID

class EwConnect : ModInitializer {

    companion object {
        lateinit var server: MinecraftServer
        lateinit var events: EventHandlers
        lateinit var spark: Spark
        lateinit var hologramManager: HologramManager
        
        lateinit var graveyardCommands: GraveyardCommands
        var discordBot: DiscordBot? = null
        val sessionTimes = mutableMapOf<UUID, Long>()

        val activeEvents: List<IServerEvent> = mutableListOf(
            TreasureHuntEvent()
        )

        fun getEveryPlayerEver(): Collection<GameProfile> {
            val cacheFile = server.serverDirectory.resolve("usercache.json")
            val profiles = mutableListOf<GameProfile>()

            val reader = Files.newBufferedReader(cacheFile)
            val array = JsonParser.parseReader(reader)

            try {
                array.asJsonArray.forEach { entry ->
                    val obj = entry.asJsonObject
                    val name = obj.get("name").asString
                    val uuid = UUID.fromString(obj.get("uuid").asString)
                    profiles.add(GameProfile(uuid, name))
                }
            } catch (exception: Exception) {
                ChatUtils.sendMessage("<red>Reading players failed: ${exception}")
            }

            return profiles;
        }

    }

    override fun onInitialize() {
        events = EventHandlers(this)
        val commandManager =
            FabricServerCommandManager(ExecutionCoordinator.simpleCoordinator(), SenderMapper.identity())

        Commands(commandManager)
        graveyardCommands = GraveyardCommands(commandManager)
        TreasureHuntCommands(commandManager)
        sessionTimes.clear()
    }
    
}
