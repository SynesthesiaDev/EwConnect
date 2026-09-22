package dev.synesthesia.ewconnect

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import dev.synesthesia.ewconnect.commands.AdminCommands
import dev.synesthesia.ewconnect.commands.Commands
import dev.synesthesia.ewconnect.commands.GraveyardCommands
import dev.synesthesia.ewconnect.commands.TreasureHuntCommands
import dev.synesthesia.ewconnect.commands.TrollCommands
import dev.synesthesia.ewconnect.discord.DiscordBot
import dev.synesthesia.ewconnect.entities.HologramManager
import dev.synesthesia.ewconnect.event.IServerEvent
import dev.synesthesia.ewconnect.event.paige.MiningFrenzy
import dev.synesthesia.ewconnect.items.ICustomItem
import dev.synesthesia.ewconnect.items.RecoveryEcho
import me.lucko.spark.api.Spark
import net.fabricmc.api.ModInitializer
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
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

        val activeEvents: List<IServerEvent> = mutableListOf(MiningFrenzy())
        
        val customItems: List<ICustomItem> = mutableListOf(RecoveryEcho())

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

        fun getMainLevel(): ServerLevel {
            val resourceLocation = Identifier.parse("overworld")
            val levelKey = ResourceKey.create(Registries.DIMENSION, resourceLocation)

            return server.getLevel(levelKey)!!
        }
        
        fun getRegistries(): RegistryAccess {
            return getMainLevel().registryAccess()
        }
    }

    override fun onInitialize() {
        events = EventHandlers(this)
        val commandManager =
            FabricServerCommandManager(ExecutionCoordinator.simpleCoordinator(), SenderMapper.identity())

        Commands(commandManager)
        graveyardCommands = GraveyardCommands(commandManager)
        TreasureHuntCommands(commandManager)
        TrollCommands(commandManager)
        AdminCommands(commandManager)

        activeEvents.forEach { e -> e.registerCommands(commandManager) }

        sessionTimes.clear()
    }
}
