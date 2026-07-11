package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.entities.OfflinePlayer
import dev.synesthesia.ewconnect.extensions.location
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.graveyard.GraveyardInfo
import dev.synesthesia.ewconnect.graveyard.GraveyardManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.parser.standard.StringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class GraveyardCommands(manager: FabricServerCommandManager<CommandSourceStack>) {

    lateinit var cached: List<OfflinePlayer>

    private fun getSuggestions(): BlockingSuggestionProvider.Strings<CommandSourceStack> {
        return BlockingSuggestionProvider.Strings { _, _ -> cached.map { it.username } }
    }
    
    fun cache() {
        cached = EwConnect.getEveryPlayerEver().map { profile -> OfflinePlayer.fromGameProfile(profile) }
    }

    init {
        val base = manager.commandBuilder("graveyard")

        manager.command(
            base.literal("create")
                .required("username", stringParser(), getSuggestions())
                .required("epitaph", stringParser(StringParser.StringMode.QUOTED), getSuggestions())
                .handler { context ->
                    val player = context.sender().player ?: return@handler
                    val username = context.get<String>("username")
                    val epitaph = context.get<String>("epitaph")
                    
                    if (!player.permissions().hasPermission(Permission.HasCommandLevel(PermissionLevel.ADMINS))) {
                        player.send("<red>You do not have access to this command, only maya has :3")
                        return@handler
                    }

                    GraveyardManager.createOrUpdate(username, player.location.subtract(0.0, 1.975, 0.0), epitaph)
                }
        )
    }
}