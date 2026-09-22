package dev.synesthesia.ewconnect.event.paige

import dev.synesthesia.ewconnect.extensions.location
import net.minecraft.commands.CommandSourceStack
import org.incendo.cloud.fabric.FabricServerCommandManager

class MiningFrenzyCommands(manager: FabricServerCommandManager<CommandSourceStack>) {
    init {
        val base = manager.commandBuilder("mining")

        manager.command(
            base.literal("spawn_merchant")
                .permission("ew.admin")
                .handler { context ->
                    var player = context.sender().player!!

                    MiningFrenzyItems.spawnTrader(player.location)
                }
        )

    }
}