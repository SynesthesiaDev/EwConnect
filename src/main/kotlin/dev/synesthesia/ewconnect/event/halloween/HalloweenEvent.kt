package dev.synesthesia.ewconnect.event.halloween

import dev.synesthesia.ewconnect.event.IServerEvent
import dev.synesthesia.ewconnect.extensions.location
import dev.synesthesia.ewconnect.extensions.sendPrivateSound
import dev.synesthesia.ewconnect.utils.FabricScheduler
import dev.synesthesia.ewconnect.utils.Location
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import org.incendo.cloud.fabric.FabricServerCommandManager

// IDEAS
// flickering jhack o lanterns when ur near
//

class HalloweenEvent : IServerEvent {

    companion object {
        val SPAWN = Location(-7, 69, 2, "minecraft:overworld")
    }

    override val title: String = "Halloween"

    override val overridesMotd: String =
        "<#ac75ff>★ <#8e4eed><bold>EW NATION</bold> <#ac75ff>★\n<#a3a3a3>a very spooky <#ff8000>Halloween<#a3a3a3>!"

    override fun onPlayerJoin(player: ServerPlayer) {
        player.sendPrivateSound(SoundEvents.BELL_RESONATE, 1f, 0f)
        player.sendPrivateSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1f, 0f)
    }

    override fun onInit(server: MinecraftServer) {
        val spawnWorld = SPAWN.serverLevel 
        val radius = 8.0

        FishingMinigame.init()

        FabricScheduler.runRepeating(0, 30 * 20) {

            FabricScheduler.repeatWithDelay(3, 0) {
                val ghost = SpoopyGhost()
                ghost.spawn(SPAWN)
            }
            
            return@runRepeating true
        }
        
        FabricScheduler.runRepeating(0, 2) {

            spawnWorld.sendParticles(
                ParticleTypes.SOUL,
                SPAWN.x, SPAWN.y, SPAWN.z,
                2,
                radius, 0.0, radius, 
                0.0
            )

            return@runRepeating true
        }
    }

    override fun registerCommands(commandManager: FabricServerCommandManager<CommandSourceStack>) {
        HalloweenCommands(commandManager)
    }
}