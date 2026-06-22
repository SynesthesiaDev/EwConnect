package dev.synesthesia.ewconnect.commands

import dev.minn.jda.ktx.interactions.commands.Option
import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.event.treasurehunt.TreasureHuntEvent
import dev.synesthesia.ewconnect.extensions.color
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.extensions.sendPrivateSound
import dev.synesthesia.ewconnect.extensions.teleport
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.sounds.SoundEvents
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class TreasureHuntCommands(manager: FabricServerCommandManager<CommandSourceStack>) {

    init {
        manager.buildAndRegister("hint") {
            handler { context ->
                val player = context.sender().player ?: return@handler
                val data = Database.getTreasureHuntData(player.uuid)
                val x = player.position().x + 0.5
                val y = player.position().y + 0.5
                val z = player.position().z + 0.5

                if (data.finished) {
                    player.send("<red>You already finished the treasure hunt, silly")
                    return@handler
                }
                if (!data.started) {
                    player.send("<gold><bold>Treasure Hunt!</bold> <gray>Come to the treasure chest at spawn to start!")
                    return@handler
                }
                val nextHint = data.nextHint.getOrNull() ?: return@handler
                player.sendPrivateSound(SoundEvents.CHICKEN_EGG, x, y, z, 1.0f, 2.0f)
                player.send(" ")
                player.send(" <yellow><bold>Hint for next one:")
                player.send(" <white>${nextHint.hint}")
                player.send(" ")
            }
        }

        manager.buildAndRegister("treasurehunt_admin_tp") {
            handler { context ->
                val player = context.sender().player ?: return@handler

                if (!player.permissions().hasPermission(Permission.HasCommandLevel(PermissionLevel.ADMINS))) {
                    player.send("<red>You do not have access to this command, only maya has :3")
                    return@handler
                }

                val data = Database.getTreasureHuntData(player.uuid)

                if (data.finished) {
                    player.send("<red>You already finished the treasure hunt, silly")
                    return@handler
                }
                if (!data.started) {
                    player.teleport(TreasureHuntEvent.STARTING_BLOCK_POS)
                    return@handler
                }

                player.teleport(data.nextHint.get().blockPos)
            }
        }

        manager.buildAndRegister("treasurehunt_admin_reset") {

            handler { context ->
                val player = context.sender().player ?: return@handler

                if (!player.permissions().hasPermission(Permission.HasCommandLevel(PermissionLevel.ADMINS))) {
                    player.send("<red>You do not have access to this command, only maya has :3")
                    return@handler
                }

                Database.editTreasureHuntData(player.uuid) { data ->
                    data.nextHint = Optional.empty()
                    data.started = false
                    data.finished = false
                    data.collectedHints.clear()
                }

                TreasureHuntEvent.assignNextHint(player)
            }
        }
    }
}