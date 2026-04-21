package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.extensions.send
import revxrsal.commands.annotation.Command
import revxrsal.commands.fabric.actor.FabricCommandActor

class GravesCommand {
    @Command("graves")
    fun getGraves(actor: FabricCommandActor) {
        val player = actor.asPlayer() ?: throw Exception("Only players can execute this")

        val graves = Database.getGraves(player.uuid)
        if (graves.isEmpty()) {
            player.send("<red>You do not have any graves!")
            return
        }

        player.send(" ")
        player.send("<gold>Your graves:")
        graves.forEach { grave ->
            player.send("<gray> - <aqua>${grave.location.toShortString()} <gray>in <aqua>${grave.world} <yellow>(${grave.items.size} items)")
        }
    }
}