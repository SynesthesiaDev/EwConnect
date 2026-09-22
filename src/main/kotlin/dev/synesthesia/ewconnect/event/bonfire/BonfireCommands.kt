package dev.synesthesia.ewconnect.event.bonfire

import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.event.bonfire.BonfireFestivalEvent.Companion.EMBERS_REQUIRED
import dev.synesthesia.ewconnect.extensions.giveOrDrop
import dev.synesthesia.ewconnect.extensions.location
import dev.synesthesia.ewconnect.extensions.send
import net.minecraft.commands.CommandSourceStack
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector
import org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers

class BonfireCommands(manager: FabricServerCommandManager<CommandSourceStack>) {

    init {
        val base = manager.commandBuilder("bonfire")

        manager.buildAndRegister("festival") {
            handler { context ->
                val player = context.sender().player ?: return@handler
                player.send("<#f56c42>\uD83D\uDD25 <yellow>The festival is located on the left of spawn, near the lake!")
            }
        }

        manager.command(
            base.literal("reset")
                .required("player", VanillaArgumentParsers.singlePlayerSelectorParser())
                .permission("ew.admin")
                .handler { context ->
                    val executor = context.sender().player!!
                    val player = (context["player"] as SinglePlayerSelector).single()
                    Database.bonfireFestivalData.remove(player.uuid)

                    executor.send("<red>Reset ${player.name}'s progress")
                }
        )

        manager.command(
            base.literal("reset_today")
                .required("player", VanillaArgumentParsers.singlePlayerSelectorParser())
                .permission("ew.admin")
                .handler { context ->
                    val executor = context.sender().player!!
                    val player = (context["player"] as SinglePlayerSelector).single()
                    Database.editBonfirePlayerData(player.uuid) { data ->
                        data.bankedEmbers = 0
                        data.lastCompletionTime = 0
                        data.assignedToday = mutableListOf()
                        data.lastAssignmentTime = System.currentTimeMillis()
                        data.assignedToday =
                            BonfireFestivalEvent.getAssignedToday(data.flamesCollectedRecently).shuffled()
                                .take(EMBERS_REQUIRED).map { flame -> flame.id() }
                                .toMutableList()
                    }

                    executor.send("<red>Reset ${player.name}'s today progress")
                }
        )

        manager.command(
            base.literal("give_ember")
                .permission("ew.admin")
                .handler { context ->
                    val executor = context.sender().player!!
                    executor.giveOrDrop(BonfireFestivalEvent.FESTIVAL_TICKET_ITEM_STACK)
                }
        )

        manager.command(
            base.literal("spawn_trader")
                .permission("ew.admin")
                .handler { context ->
                    val location = context.sender().player!!.location
                    FestivalItems.spawnTrader(location)
                }
        )

    }
}