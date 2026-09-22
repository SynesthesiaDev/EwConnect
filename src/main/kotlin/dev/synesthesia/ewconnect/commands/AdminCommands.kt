package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.extensions.giveOrDrop
import dev.synesthesia.ewconnect.extensions.location
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.graveyard.GraveyardManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class AdminCommands(manager: FabricServerCommandManager<CommandSourceStack>) {

    private fun suggestCustomItems(): BlockingSuggestionProvider.Strings<CommandSourceStack> {
        return BlockingSuggestionProvider.Strings { _, _ -> EwConnect.customItems.map { it.identifier } }
    }

    init {
        val base = manager.commandBuilder("admin")

        manager.command(
            base.literal("item")
                .required("identifier", stringParser(), suggestCustomItems())
                .permission("ew.admin")
                .handler { context ->
                    val player = context.sender().player ?: return@handler
                    val identifier = context.get<String>("identifier")
                    val item = EwConnect.customItems.firstOrNull { it.identifier == identifier }
                    if (item == null) {
                        player.send("<red>(!) Custom item with identifier <#ff0000>${identifier}<red> does not exist!")
                        return@handler;
                    }
                    
                    player.giveOrDrop(item.itemStack)
                }
        )
    }

}