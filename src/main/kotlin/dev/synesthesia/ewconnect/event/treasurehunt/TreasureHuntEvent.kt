package dev.synesthesia.ewconnect.event.treasurehunt

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.event.IServerEvent
import dev.synesthesia.ewconnect.extensions.formattedChatNickname
import dev.synesthesia.ewconnect.extensions.formattedDiscordNickname
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.extensions.sendPrivateParticles
import dev.synesthesia.ewconnect.extensions.sendPrivateSound
import dev.synesthesia.ewconnect.utils.FabricScheduler
import dev.synesthesia.ewconnect.utils.item
import io.netty.buffer.ByteBuf
import net.kyori.adventure.bossbar.BossBar
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class TreasureHuntEvent : IServerEvent {

    override val title: String = "Treasure Hunt"

    companion object {
        val TREASURES: List<Hint> = listOf(
            Hint(
                BlockPos(-17, 82, -24),
                "Search for these helpful friends, in their abode above where they trade and sleep is where you’ll find me."
            ),
            Hint(
                BlockPos(22, 54, -69),
                "You must head to the deep below far below the earths crust where its full of a red and orange glow, the location you’ll find your me is where all roads lead with an army of undead.",
                true
            ),
            Hint(
                BlockPos(26, 70, 64),
                "Find me in a place we all love, a place so colorful and full of nourishment from the land. You’ll find me hidden on the ground but I can see many colors and people around."
            ),
            Hint(
                BlockPos(75, 80, 168),
                "I hide somewhere so clear and nice, you pass me but dont realize I’m right in your front of your eyes. Higher than expected thanks to a friend but close to the ground is where I am. The world is beautiful when looking through a heart-shaped rose colored lens."
            ),
            Hint(
                BlockPos(31, 97, -93),
                "Here I rest and eat never enough to fill so the rain I must drink. I watch over you all as you sleep, be not afraid of my teeth."
            ),
            Hint(
                BlockPos(98, 72, -15),
                "I grow and decay with passing seasons but provide for all. I have dressed in colors that fit a theme."
            ),
            Hint(
                BlockPos(56, 74, -73),
                "Here you come and go like the wind and I brew and steep as you walk in. Care for a cookie?"
            ),
            Hint(
                BlockPos(-95, 133, -1),
                "I am hidden in a place full of colors and pride. Many people fear me.. are you sure that you aren’t one?"
            ),
            Hint(
                BlockPos(166, 73, 252),
                "Lost and forgotten I sit but many still need what I have, you spin and spin until you can’t any more but what you realize is I have many uses for my eyes and more."
            ),
            Hint(
                BlockPos(-177, -49, -131),
                "Forgotten by time for committing a sin. People look for the secrets I hold within, deep in the depths you’ll find me but be careful for a creature guards me."
            ),
            Hint(
                BlockPos(2, 255, -6),
                "Home to many squealing friends. You’ll find me above where the shimmering gold flows in. Here when you collect you hear a peculiar ding. No better place to fix your tools but here is where you begin. ",
                true
            ),
            Hint(
                BlockPos(30, 79, -74),
                "You’ve run past me many times but not once have you saw through my guise. At the peak of a watery plain you’ll find me away from my colorful friends."
            ),
            Hint(
                BlockPos(881, 113, 356),
                "One last spot until you find the treasure, to find anything you’ll need this tool’s help! The best way to find it is to remember we’re all being tracked. There’s no better navigation tool than this ___.",
                isNether = false,
                isFinal = true
            )
        )

        val FINAL_HINT = TREASURES.last()
        val STARTING_BLOCK_POS = BlockPos(-26, 70, 10)

        fun assignNextHint(player: ServerPlayer) {
            Database.editTreasureHuntData(player.uuid) { data ->

                val current = data.nextHint.getOrNull()
                if (current != null) data.collectedHints.add(current)

                val nextHint: Hint = if (data.collectedHints.size < 6)
                    TREASURES.filter { hint ->
                        hint.id() !in data.collectedHints.map { it.id() } && !hint.isFinal
                    }.random()
                else FINAL_HINT

                data.nextHint = Optional.of(nextHint)
            }
        }
    }

    data class Hint(
        val blockPos: BlockPos,
        val hint: String,
        val isNether: Boolean = false,
        val isFinal: Boolean = false,
    ) {
        companion object {
            fun fromId(id: Int): Hint = TREASURES[id]

            val STREAM_CODEC: StreamCodec<ByteBuf, Hint> = ByteBufCodecs.VAR_INT.map(
                { id -> fromId(id) },
                { hint -> hint.id() }
            )
        }

        fun id(): Int = TREASURES.indexOf(this)
    }

    @Suppress("SENSELESS_COMPARISON")
    override fun onPlayerJoin(player: ServerPlayer) {

        val bossbar: BossBar = BossBar.bossBar(
            ChatUtils.translated("<gold><bold>Treasure Hunt!</bold> <gray>Use <white>/hint <gray>to see more"),
            1f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.NOTCHED_12
        )

        bossbar.addViewer(player)

        val ticks = 10 * 20
        FabricScheduler.repeatWithDelay(times = ticks, delayTicks = 1) { index ->
            if (!player.isAlive || player.connection == null) return@repeatWithDelay

            val ticksRemaining = ticks - (index + 1)
            val progress = ticksRemaining.toFloat() / ticks.toFloat()

            bossbar.progress(progress.coerceIn(0f, 1f))

            if (ticksRemaining <= 0) {
                bossbar.removeViewer(player)
            }
        }
    }

    override fun onPlayerLeave(player: ServerPlayer) {
    }

    fun onPlayerClickOnBlock(player: ServerPlayer, blockPos: BlockPos) {
        var data = Database.getTreasureHuntData(player.uuid)

        if (data.finished) return
        val x = blockPos.x.toDouble() + 0.5
        val y = blockPos.y.toDouble() + 0.5
        val z = blockPos.z.toDouble() + 0.5

        if (!data.started && blockPos == STARTING_BLOCK_POS) {

            ChatUtils.sendMessage(
                "<gold>(Treasure Hunt) <gray>${player.formattedChatNickname} <gray>has started their treasure hunt journey, good luck!",
                player
            )
            
            EwConnect.discordBot?.onTreasureHunt(player.uuid, "${player.formattedDiscordNickname} has started the treasure hunt!")

            player.send(" ")
            player.send(" <gold><bold>Treasure Hunt!")
            player.send(" ")
            player.send(" <gray>Welcome to the treasure hunt event!")
            player.send(" <gray>Collect <yellow>5 hints<gray> to get to the final treasure!")
            player.send(" <gray>You can always check your current hint using <aqua>/hint")
            player.send(" ")

            player.sendPrivateSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, x, y, z, 1.0f, 2.0f)
            player.sendPrivateParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 30, 0.5f)

            assignNextHint(player)
            Database.editTreasureHuntData(player.uuid) { huntData ->
                huntData.started = true
            }

            data = Database.getTreasureHuntData(player.uuid)

            FabricScheduler.runLater(6 * 20) {
                player.sendPrivateSound(SoundEvents.CHICKEN_EGG, x, y, z, 1.0f, 2.0f)
                player.send(" ")
                player.send(" <yellow><bold>Hint for your first location:")
                player.send(" <white>${data.nextHint.get().hint}")
                player.send(" ")
            }

            return
        }

        val nextHint = data.nextHint.getOrNull() ?: return

        if (nextHint.blockPos != blockPos) return


        data = Database.getTreasureHuntData(player.uuid)

        if (nextHint.isFinal) {
            player.send("<gold><bold>YIPPEE!</bold> <gray>You found the final treasure!")
            ChatUtils.sendMessage(
                "<gold>(Treasure Hunt) <gray>${player.formattedChatNickname} <gray>has found <gold>the one piece<gray> (treasure)! Congrats!!",
                player
            )
            EwConnect.discordBot?.onTreasureHunt(player.uuid, "${player.formattedDiscordNickname} has finished the treasure hunt and found the treasure! ggwp")
            
            player.sendPrivateSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, x, y, z, 1.0f, 2.0f)
            player.sendPrivateParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 30, 0.5f)

            Database.editTreasureHuntData(player.uuid) { huntData ->
                huntData.finished = true
            }

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
                    player.drop(item, false)
                    player.send("<red>(!) Your inventory is fully so an item was dropped to the ground")
                }
            }

        } else {
            player.sendPrivateSound(SoundEvents.EXPERIENCE_ORB_PICKUP, x, y, z, 1.0f, 1.0f)
            player.sendPrivateSound(SoundEvents.ENCHANTMENT_TABLE_USE, x, y, z, 1.0f, 1.0f)
            player.sendPrivateParticles(ParticleTypes.ENCHANT, x, y, z, 40, 0.3f)

            player.send(" <gold><bold>Hint Found!</bold> <gray>You found a hint! <aqua>(${data.collectedHints.size}/5)")
            ChatUtils.sendMessage(
                "<gold>(Treasure Hunt) <gray>${player.formattedChatNickname} <gray>has found a treasure hint! <yellow>(${data.collectedHints.size}/5)",
                player
            )
            EwConnect.discordBot?.onTreasureHunt(player.uuid, "${player.formattedDiscordNickname} has found a treasure hint! **(${data.collectedHints.size}/5)**")

            assignNextHint(player)
            data = Database.getTreasureHuntData(player.uuid)

            FabricScheduler.runLater(3 * 20) {
                player.sendPrivateSound(SoundEvents.CHICKEN_EGG, x, y, z, 1.0f, 2.0f)
                player.send(" ")
                player.send(" <yellow><bold>Hint for next one:")
                player.send(" <white>${data.nextHint.get().hint}")
                player.send(" ")
            }
        }
    }

    
}