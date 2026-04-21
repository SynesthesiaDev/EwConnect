package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.extensions.send
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import revxrsal.commands.annotation.Command
import revxrsal.commands.fabric.actor.FabricCommandActor

class AdminGravesCommand {
    @Command("admin_graves")
    fun getGraves(actor: FabricCommandActor) {
        val player = actor.asPlayer() ?: throw Exception("Only players can execute this")

        if (!player.permissions().hasPermission(Permission.HasCommandLevel(PermissionLevel.ADMINS))) {
            player.send("<red>You do not have access to this command, only maya has :3")
            return
        }

        val graves = Database.getAllGraves()
        if (graves.isEmpty() || graves.all { grave -> grave.value.isEmpty() }) {
            player.send("<red>There are no graves!")
            return
        }

        Database.getAllGraves().forEach { (_, graves) ->
            val first = graves.firstOrNull() ?: return@forEach

            player.send(" ")
            player.send("<gold> ${first.lastKnowUsername}:")
            graves.forEach { grave ->
                player.send("<gray> - <aqua>${grave.location.toShortString()} <gray>in <aqua>${grave.world} <yellow>(${grave.items.size} items)")
            }
        }
    }
}