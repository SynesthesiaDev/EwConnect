package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.extensions.color
import dev.synesthesia.ewconnect.extensions.send
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import revxrsal.commands.annotation.Command
import revxrsal.commands.fabric.actor.FabricCommandActor

class AdminNicknameCommand {
    @Command("forcenickname <player>")
    fun forceNickname(actor: FabricCommandActor, player: ServerPlayer, name: String) {
        val executingPlayer = actor.asPlayer() ?: throw Exception("Only players can execute this")
        
        if(!executingPlayer.permissions().hasPermission(Permission.HasCommandLevel(PermissionLevel.ADMINS))) {
            executingPlayer.send("<red>You do not have access to this command!")
            return
        }

        Database.setNickname(player.uuid, name)
        val color = player.color
        executingPlayer.send("<green>Set nickname of <white>${player.plainTextName} to <${color}>${name}<green>!")
    }
}