package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.extensions.color
import dev.synesthesia.ewconnect.extensions.isValidHexColor
import dev.synesthesia.ewconnect.extensions.send
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.StringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser

class Commands(manager: FabricServerCommandManager<CommandSourceStack>) {

    init {

        manager.buildAndRegister("nickname") {
            required("name", stringParser())
            handler { context ->
                val player = context.sender().player ?: return@handler
                val name: String = context["name"]
                if (name.length > 16) {
                    player.send("<red>nickname cannot be longer than 16 characters!")
                    return@handler
                }

                Database.setNickname(player.uuid, name)
                player.send("<green>Set your nickname to <${player.color}>${name}<green>!")
            }
        }

        manager.buildAndRegister("color") {
            required("color", stringParser(StringParser.StringMode.GREEDY))
            handler { context ->
                val hex: String = context["color"]
                val player = context.sender().player ?: return@handler

                if (!hex.isValidHexColor) {
                    player.send("<red>'${hex}' is not valid hex color code!")
                    return@handler
                }

                Database.setColor(player.uuid, hex)
                player.send("<green>Set your color to <${hex}>${hex}<green>!")
            }
        }

        manager.buildAndRegister("graves") {
            handler { context ->
                val player = context.sender().player ?: return@handler

                val graves = Database.getGraves(player.uuid)
                if (graves.isEmpty()) {
                    player.send("<red>You do not have any graves!")
                    return@handler
                }

                player.send(" ")
                player.send("<gold>Your graves:")
                graves.forEach { grave ->
                    player.send("<gray> - <aqua>${grave.location.toShortString()} <gray>in <aqua>${grave.world} <yellow>(${grave.items.size} items)")
                }
            }
        }

        manager.buildAndRegister("admin_graves") {
            handler { context ->
                val player = context.sender().player ?: return@handler

                if (!player.permissions().hasPermission(Permission.HasCommandLevel(PermissionLevel.ADMINS))) {
                    player.send("<red>You do not have access to this command, only maya has :3")
                    return@handler
                }

                val graves = Database.getAllGraves()
                if (graves.isEmpty() || graves.all { grave -> grave.value.isEmpty() }) {
                    player.send("<red>There are no graves!")
                    return@handler
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
    }
}