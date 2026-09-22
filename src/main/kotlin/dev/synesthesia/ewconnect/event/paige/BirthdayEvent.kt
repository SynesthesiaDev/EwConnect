package dev.synesthesia.ewconnect.event.paige

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.event.IServerEvent
import dev.synesthesia.ewconnect.extensions.playSound
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.extensions.swing
import dev.synesthesia.ewconnect.utils.FabricScheduler
import dev.synesthesia.ewconnect.utils.Location
import net.kyori.adventure.bossbar.BossBar
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.Mannequin
import net.minecraft.world.item.component.SwingAnimation
import net.minecraft.world.level.block.Blocks
import org.geysermc.floodgate.api.FloodgateApi
import org.incendo.cloud.fabric.FabricServerCommandManager
import java.util.Random
import java.util.UUID

class BirthdayEvent() : IServerEvent {


    override val overridesMotd: String =
        "<#f475ff>★ <#eb008d><bold>EW NATION</bold> <#f475ff>★\n<#a3a3a3>${PERSON}'s birthday event!!"

    override val title: String = "${PERSON}'s Birthday"

    val cakeSlices: MutableMap<UUID, Int> = mutableMapOf()


    companion object {
        const val PERSON: String = "Paige"
        const val HOLOGRAM_KEY = "birthday_event_hologram"

        val MESSAGES: List<String> = listOf(
            "You enjoy a nice slice of cake",
            "You inhale the whole slice in one go",
            "You devour the slice of cake like your life depends on it",
            "Yum! Whats better than free food..",
            "You enjoy a nice slice of cak- wait what. you find a pice of flesh inside. huh"
        )

        val HOLOGRAM_POSITION = Location(5.5, 66.5, -26.5, "minecraft:overworld")
    }

    override fun onInit(server: MinecraftServer) {

        val hologramManager = EwConnect.hologramManager
        hologramManager.remove(HOLOGRAM_KEY)
        val world = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse("minecraft:overworld")))!!

        hologramManager.create(HOLOGRAM_KEY) {
            setLocation(HOLOGRAM_POSITION)

            addStatic("<#ffa1c8>❤ <bold>${PERSON}'s Birthday!!</bold> ❤")
            addStatic("")
            addStatic("Happy birthday $PERSON dear <3")
            addStatic("Go click on everyone's NPC")
            addStatic("to see what they have to say!!")
            addStatic("")
            addStatic("")
            addStatic("<aqua>⛏ <bold>Mining Frenzy </bold>⛏")
            addStatic("")
            addStatic("While mining ore, there is a chance to")
            addStatic("drop a number of <#c8a6ff>Mining Shards")
            addStatic("")
            addStatic("You can exchange <#c8a6ff>Mining Shards<white> for rewards")
            addStatic("at the <yellow>event shop<white>!")
            addStatic("")
            addStatic("The rarer the ore, the bigger chance to get")
            addStatic("more <#c8a6ff>Mining Shards<white>!")
            addStatic("")
        }
    }

    override fun onPlayerJoin(player: ServerPlayer) {

        if (FloodgateApi.getInstance().isFloodgatePlayer(player.uuid)) return

        val bossbar: BossBar = BossBar.bossBar(
            ChatUtils.translated("<#ffa1c8>❤ <bold>${PERSON}'s Birthday!!</bold> ❤"),
            1f,
            BossBar.Color.PINK,
            BossBar.Overlay.NOTCHED_6
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

        FabricScheduler.runRepeating(0, (5 * 60) * 20) {
            cakeSlices.clear()
            return@runRepeating true;
        }
    }

    fun onPlayerClickOnBlock(player: ServerPlayer, blockPos: BlockPos, world: ServerLevel) {
        if (world.getBlockState(blockPos).block == Blocks.BARRIER) {

            if (cakeSlices.getOrPut(player.uuid) { 0 } >= 5) {
                player.send("<gray><italic>The cake whispers to you: You already had 5 slices you fatass..")
                return
            }

            player.foodData.foodLevel = 20
            player.playSound(SoundEvents.PLAYER_BURP, 2f, Random().nextFloat(0.9f, 1.5f), SoundSource.MASTER)
            player.send("<gray><italic>${MESSAGES.random()}")

            if (!cakeSlices.contains(player.uuid)) {
                cakeSlices[player.uuid] = 0
            } else {
                cakeSlices[player.uuid] = cakeSlices[player.uuid]!! + 1
            }
        }
    }

    fun onEntityInteraction(player: ServerPlayer, level: ServerLevel, hand: InteractionHand, entity: Entity) {
        if (hand == InteractionHand.OFF_HAND) return

        if (entity is Mannequin && entity.entityTags().isNotEmpty()) {
            val tag = entity.entityTags().first()
            val npc = BirthdayNpcs.MESSAGES[tag]!!
            player.send(" ")
            player.send("<#ebc634><bold>${npc.name}:</bold> <#fff3c2>${npc.message}")
            player.playSound(SoundEvents.VILLAGER_TRADE, 2f, Random().nextFloat(0.9f, 1.5f), SoundSource.MASTER)

            entity.swing()
        }
    }

    override fun registerCommands(commandManager: FabricServerCommandManager<CommandSourceStack>) {
        BirthdayCommands(commandManager)
    }
}