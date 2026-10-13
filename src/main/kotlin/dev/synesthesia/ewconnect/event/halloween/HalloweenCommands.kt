package dev.synesthesia.ewconnect.event.halloween

import dev.synesthesia.ewconnect.extensions.location
import net.minecraft.commands.CommandSourceStack
import org.incendo.cloud.fabric.FabricServerCommandManager

class HalloweenCommands(manager: FabricServerCommandManager<CommandSourceStack>) {

    init {
        val base = manager.commandBuilder("halloween")

        manager.command(
            base.literal("ghost")
                .permission("ew.admin")
                .handler { context ->
                    var player = context.sender().player!!
                    var ghost = SpoopyGhost()
                    
                    ghost.spawn(player.location)
                }
        )
    }
}