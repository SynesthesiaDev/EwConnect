package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.event.treasurehunt.TreasureHuntEvent
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.extensions.sendPrivateSound
import dev.synesthesia.ewconnect.extensions.teleport
import dev.synesthesia.ewconnect.utils.item
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Prediction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector
import org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

        manager.buildAndRegister("treasurehunt_admin_getrwards") {
            required("player", VanillaArgumentParsers.singlePlayerSelectorParser())
            handler { context ->
                val player = (context["player"] as SinglePlayerSelector).single()

                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                val current = LocalDateTime.now().format(formatter)
                val playerRewards = mutableListOf(
                    ItemStack(Items.DIAMOND, 4),
                    ItemStack(Items.NETHERITE_SCRAP, 2),
                    item(Items.PAPER) {
                        name("<yellow><bold>Custom 2x2 Painting Voucher")
                        lore("<gray>Contact Maya to get it with", "<gray>url to an image (preferable square)!")
                        shiny()
                    },

                    item(Items.PAPER, 3) {
                        name("<aqua><bold>Custom Decorative Head Voucher")
                        lore("<gray>Contact Maya to get it!")
                        shiny()
                    },

                    item(Items.GOLDEN_NAUTILUS_ARMOR) {
                        name("<gold><bold>Treasure Hunt Trophy")
                        lore(
                            "<gray>Awarded to <white>${player.plainTextName}<gray>",
                            "<gray>on <aqua>${current}",
                            " ",
                            "<gray>Thank you for playing <light_purple>❤"
                        )
                        shiny()
                    }
                )

                playerRewards.forEach { item ->
                    if (!player.inventory.add(item)) {
                        player.drop(item, false, Prediction.PREDICTED)
                        player.send("<red>(!) Your inventory is fully so an item was dropped to the ground")
                    }
                }
            }
        }
    }
}