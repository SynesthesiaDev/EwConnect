package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.extensions.color
import dev.synesthesia.ewconnect.extensions.send
import revxrsal.commands.annotation.Command
import revxrsal.commands.fabric.actor.FabricCommandActor

class NicknameCommand {

    @Command("nickname")
    fun setNickname(actor: FabricCommandActor, name: String) {
        val player = actor.asPlayer() ?: throw Exception("Only players can execute this")

        if (name.length > 16) {
            player.send("<red>nickname cannot be longer than 16 characters!")
            return
        }

        Database.setNickname(player.uuid, name)
        player.send("<green>Set your nickname to <${player.color}>${name}<green>!")
    }
    
}
