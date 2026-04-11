package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.extensions.isValidHexColor
import dev.synesthesia.ewconnect.extensions.send
import revxrsal.commands.annotation.Command
import revxrsal.commands.fabric.actor.FabricCommandActor

class ColorCommand {

    @Command("color")
    fun setColor(actor: FabricCommandActor, hex: String) {
        val player = actor.asPlayer() ?: throw Exception("Only players can execute this")

        if(!hex.isValidHexColor) {
            player.send("<red>'${hex}' is not valid hex color code!")
            return
        }

        Database.setColor(player.uuid, hex)
        player.send("<green>Set your color to <${hex}>${hex}<green>!")
    }    
}
