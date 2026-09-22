package dev.synesthesia.ewconnect.event.paige

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.event.IServerEvent
import dev.synesthesia.ewconnect.event.paige.BirthdayEvent.Companion.HOLOGRAM_KEY
import dev.synesthesia.ewconnect.event.paige.BirthdayEvent.Companion.HOLOGRAM_POSITION
import dev.synesthesia.ewconnect.event.paige.BirthdayEvent.Companion.PERSON
import dev.synesthesia.ewconnect.extensions.actionBar
import dev.synesthesia.ewconnect.extensions.giveOrDrop
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.utils.BossbarUtil
import dev.synesthesia.ewconnect.utils.randomFloat
import net.kyori.adventure.bossbar.BossBar
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import org.incendo.cloud.fabric.FabricServerCommandManager
import kotlin.random.Random

class MiningFrenzy : IServerEvent {

    override val title: String = "Mining Frenzy"

    override val overridesMotd: String =
        "<#f475ff>★ <#eb008d><bold>EW NATION</bold> <#f475ff>★\n<#a3a3a3><#9effff>Mining Frenzy</#9effff> event!! <#ffc88a>26.3</#ffc88a> update!!"

    companion object {
        const val GEODE_CHANCE = 0.06
        const val JACKPOT_CHANCE = 0.05
    }
    
    @JvmField
    val oreTiers: Map<Block, BlockData> = mapOf(
        Blocks.COPPER_ORE to BlockData(dropChance = 0.15, minShards = 1, maxShaders = 1, rarity = Rarity.COMMON),
        Blocks.DEEPSLATE_COPPER_ORE to BlockData(dropChance = 0.20, minShards = 1, maxShaders = 1, rarity = Rarity.COMMON),

        Blocks.IRON_ORE to BlockData(dropChance = 0.30, minShards = 1, maxShaders = 2, rarity = Rarity.COMMON),
        Blocks.DEEPSLATE_IRON_ORE to BlockData(dropChance = 0.30, minShards = 1, maxShaders = 2, rarity = Rarity.COMMON),

        Blocks.GOLD_ORE to BlockData(dropChance = 0.35, minShards = 1, maxShaders = 2, rarity = Rarity.UNCOMMON),
        Blocks.DEEPSLATE_GOLD_ORE to BlockData(dropChance = 0.39, minShards = 1, maxShaders = 3, rarity = Rarity.UNCOMMON),
        Blocks.REDSTONE_ORE to BlockData(dropChance = 0.30, minShards = 1, maxShaders = 2, rarity = Rarity.UNCOMMON),
        Blocks.DEEPSLATE_REDSTONE_ORE to BlockData(dropChance = 0.30, minShards = 1, maxShaders = 3, rarity = Rarity.UNCOMMON),
        Blocks.LAPIS_ORE to BlockData(dropChance = 0.35, minShards = 1, maxShaders = 3, rarity = Rarity.UNCOMMON),
        Blocks.DEEPSLATE_LAPIS_ORE to BlockData(dropChance = 0.40, minShards = 1, maxShaders = 3, rarity = Rarity.UNCOMMON),

        Blocks.DIAMOND_ORE to BlockData(dropChance = 0.75, minShards = 1, maxShaders = 5, rarity = Rarity.RARE),
        Blocks.DEEPSLATE_DIAMOND_ORE to BlockData(dropChance = 0.70, minShards = 2, maxShaders = 5, rarity = Rarity.RARE),
        Blocks.EMERALD_ORE to BlockData(dropChance = 0.90, minShards = 1, maxShaders = 2, rarity = Rarity.RARE),
        Blocks.DEEPSLATE_EMERALD_ORE to BlockData(dropChance = 0.90, minShards = 1, maxShaders = 2, rarity = Rarity.RARE),
    )

    override fun onInit(server: MinecraftServer) {
        val hologramManager = EwConnect.hologramManager
        hologramManager.remove(HOLOGRAM_KEY)
        val world = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse("minecraft:overworld")))!!

        hologramManager.create(HOLOGRAM_KEY) {
            setLocation(HOLOGRAM_POSITION)

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
    
    fun onBlockMined(block: Block, player: ServerPlayer) {
        val tier = oreTiers[block] ?: return
        if (Random.nextDouble() > tier.dropChance) return

        var amount = Random.nextInt(tier.minShards, tier.maxShaders + 1)
        var isJackpot = false

        if (Random.nextDouble() < JACKPOT_CHANCE) {
            amount *= 2
            isJackpot = true
        }

        player.giveOrDrop(MiningFrenzyItems.SHARD_ITEM.copyWithCount(amount))
        playPayoutFeedback(player, tier.rarity, amount, isJackpot)

        if (Random.nextDouble() < GEODE_CHANCE) {
            player.giveOrDrop(MiningFrenzyItems.GEODE_ITEM.copy())
            playGeodeFeedback(player)
        }
    }

    private fun playGeodeFeedback(player: ServerPlayer) {
        player.level().playSound(
            null, player.blockPosition(),
            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
            1.0f, randomFloat(0.85f, 1.25f)
        )
        player.actionBar("<#7fd3ff>✧ You found a Sealed Geode!")
    }

    private fun playPayoutFeedback(player: ServerPlayer, rarity: Rarity, amount: Int, isJackpot: Boolean) {
        val level = player.level()

        level.playSound(
            null, player.blockPosition(),
            rarity.sound, SoundSource.PLAYERS,
            0.8f, rarity.pitch + randomFloat(-0.25f, 0.25f)
        )

        if (rarity == Rarity.COMMON) {
            player.actionBar("${rarity.color}+$amount Shards")
            return
        }

        level.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            player.x, player.y + 1.0, player.z,
            8, 0.3, 0.3, 0.3, 0.0
        )

        if (rarity == Rarity.UNCOMMON) {
            player.actionBar("${rarity.color}+$amount Shards")
            return
        }

        player.actionBar("${rarity.color}✦ +$amount Shards")

        if (isJackpot) {
            level.sendParticles(
                ParticleTypes.CRIT,
                player.x, player.y + 1.0, player.z,
                20, 0.4, 0.4, 0.4, 0.1
            )
            player.send("<yellow><bold>★ JACKPOT VEIN!</bold> <yellow>x2 Shards!")
        }
    }

    override fun onPlayerJoin(player: ServerPlayer) {
        val bossbar: BossBar = BossBar.bossBar(
            ChatUtils.translated("<aqua>⛏ <bold>Mining Frenzy </bold>⛏"),
            1f,
            BossBar.Color.BLUE,
            BossBar.Overlay.NOTCHED_6
        )

        BossbarUtil.show(player, bossbar)
    }
    
    data class BlockData(val dropChance: Double, val minShards: Int, val maxShaders: Int, val rarity: Rarity)

    enum class Rarity(val color: String, val sound: net.minecraft.sounds.SoundEvent, val pitch: Float) {
        COMMON("<gray>", SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2f),
        UNCOMMON("<green>", SoundEvents.EXPERIENCE_ORB_PICKUP, 0.9f),
        RARE("<aqua>", SoundEvents.PLAYER_LEVELUP, 1.4f),
        LEGENDARY("<gold>", SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f)
    }

    override fun registerCommands(commandManager: FabricServerCommandManager<CommandSourceStack>) {
        MiningFrenzyCommands(commandManager)
    }
}