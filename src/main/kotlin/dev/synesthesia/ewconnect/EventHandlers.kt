package dev.synesthesia.ewconnect

import dev.synesthesia.ewconnect.EwConnect.Companion.activeEvents
import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.database.PlayerGrave
import dev.synesthesia.ewconnect.discord.DiscordBot
import dev.synesthesia.ewconnect.entities.HologramManager
import dev.synesthesia.ewconnect.event.bonfire.BonfireFestivalEvent
import dev.synesthesia.ewconnect.event.paige.BirthdayEvent
import dev.synesthesia.ewconnect.event.paige.MiningFrenzy
import dev.synesthesia.ewconnect.event.treasurehunt.TreasureHuntEvent
import dev.synesthesia.ewconnect.extensions.toNMSComponent
import dev.synesthesia.ewconnect.extensions.formattedDiscordNickname
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.extensions.swing
import dev.synesthesia.ewconnect.extensions.toBlockPos
import dev.synesthesia.ewconnect.extensions.translated
import dev.synesthesia.ewconnect.graveyard.GraveyardManager
import dev.synesthesia.ewconnect.settings.Settings
import dev.synesthesia.ewconnect.utils.DeathCounter
import dev.synesthesia.ewconnect.utils.FabricScheduler
import dev.synesthesia.ewconnect.utils.SharedHandler
import me.lucko.spark.api.SparkProvider
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.Connection
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket
import net.minecraft.network.protocol.status.ServerStatus
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.util.Prediction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Blocks

class EventHandlers(val mod: EwConnect) {

    init {
        ServerEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (entity is ArmorStand && entity.entityTags()
                    .contains("ewconnect_cleanup_orphan") && !EwConnect.hologramManager.isActiveArmorStand(entity)
            ) {
                entity.discard()
            }
        }

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            EwConnect.server = server
            Settings.load()

            if (Settings.current.discord != null && Settings.current.discord!!.clientId != 0L) {
                EwConnect.discordBot = DiscordBot(Settings.current.discord!!)
            }
            EwConnect.spark = SparkProvider.get()
            EwConnect.hologramManager = HologramManager()
            EwConnect.graveyardCommands.cache()
            
            if(!server.scoreboard.teamNames.contains("no_push")) {
                var team = server.scoreboard.addPlayerTeam("no_push")
                server.scoreboard.onTeamAdded(team)
            }

            GraveyardManager.createHolograms()

            activeEvents.forEach { e -> e.onInit(server) }
        }

        ServerLivingEntityEvents.AFTER_DAMAGE.register { entity, _, _, _, _ ->
            if (entity.vehicle != null && entity.vehicle is Player) {
                entity.stopRiding()
            }
        }

        ServerPlayerEvents.JOIN.register { player ->
            EwConnect.discordBot?.onPlayerJoin(
                player.formattedDiscordNickname,
                player.uuid,
                player.stats.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
            )
            var scoreboard = EwConnect.server.scoreboard 
            var team = scoreboard.playerTeams.firstOrNull { t -> t.name == "no_push" }
            if(team != null) {
                scoreboard.addPlayerToTeam(player.plainTextName, team)
            }
            
            var graves = Database.getGraves(player.uuid)
            if (graves.isNotEmpty()) {
                player.send("<gray>You have <red>${graves.size} <gray>grave(s)! Check their location with <yellow>/graves")
            }
            EwConnect.sessionTimes[player.uuid] = System.currentTimeMillis()
            EwConnect.activeEvents.forEach { event -> event.onPlayerJoin(player) }
            DeathCounter.update(player)
        }

        ServerPlayerEvents.LEAVE.register { player ->
            EwConnect.discordBot?.onPlayerLeave(
                player.formattedDiscordNickname,
                player.uuid,
                EwConnect.sessionTimes[player.uuid]!!,
                player.stats.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))
            )
            EwConnect.sessionTimes.remove(player.uuid)
            EwConnect.activeEvents.forEach { event -> event.onPlayerLeave(player) }
        }

        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, f ->
            if (source.`is`(DamageTypes.FIREWORKS)) {
                return@register false
            }

            return@register true
        }

        ServerLivingEntityEvents.ALLOW_DEATH.register { player, source, f ->
            if (player is ServerPlayer) {

                val holdingTotem = player.getItemInHand(InteractionHand.MAIN_HAND).`is`(Items.TOTEM_OF_UNDYING) ||
                        player.getItemInHand(InteractionHand.OFF_HAND).`is`(Items.TOTEM_OF_UNDYING)

                if (holdingTotem) {
                    return@register true
                }

                var location = player.position().toBlockPos()
                var world = player.level()
                val inventory = player.inventory

                world.setBlock(location, Blocks.CHEST.defaultBlockState(), 3)

                val items = mutableListOf<ItemStack>()
                inventory.filter { p -> !p.isEmpty }.toList().forEach { item ->
                    items.add(item)
                    inventory.removeItem(item)
                }

                val grave = PlayerGrave(
                    player.uuid,
                    player.plainTextName,
                    location,
                    world.dimension().identifier().toString(),
                    items
                )
                Database.addGrave(grave)

                val message = source.getLocalizedDeathMessage(player).string
                val graveLoc = "`${location.toShortString()}` in **${world.dimension().identifier().toShortString()}**"

                EwConnect.discordBot?.onPlayerDeath(
                    message,
                    player.uuid,
                    player.stats.getValue(Stats.CUSTOM.get(Stats.DEATHS)),
                    graveLoc
                )

                player.send(
                    "<red>Oopsies! Your grave is at <gold>${location.toShortString()}<red> in <yellow>${
                        world.dimension().identifier().toShortString()
                    }<red>!"
                )
                player.send("<italic><gray>You can check your graves with <yellow>/graves<gray>!")
                DeathCounter.update(player)
            }

            return@register true
        }

        UseItemCallback.EVENT.register { player, world, hand ->
            if (hand != InteractionHand.MAIN_HAND) return@register InteractionResult.PASS

            val serverPlayer = player as? ServerPlayer ?: return@register InteractionResult.PASS
            val stack = player.getItemInHand(hand)

            EwConnect.customItems.forEach { item ->
                if(ItemStack.isSameItemSameComponents(item.itemStack, stack)) {
                    item.onRightClick(player)
                }
            }
            
            activeEvents.forEach { event ->
                if (event is BonfireFestivalEvent) {
                    if (event.onRightClickWithItem(serverPlayer, stack)) {
                        return@register InteractionResult.CONSUME
                    }
                }
            }

            return@register InteractionResult.PASS
        }

        UseBlockCallback.EVENT.register { player, level, hand, result ->
            if (hand != InteractionHand.MAIN_HAND) return@register InteractionResult.PASS

            activeEvents.forEach { event ->
                if (event is TreasureHuntEvent) event.onPlayerClickOnBlock(player as ServerPlayer, result.blockPos)
                if (event is BonfireFestivalEvent) event.onPlayerClickOnBlock(
                    player as ServerPlayer,
                    result.blockPos,
                    level as ServerLevel
                )
                if (event is BirthdayEvent) event.onPlayerClickOnBlock(
                    player as ServerPlayer,
                    result.blockPos,
                    level as ServerLevel
                )
            }

            return@register InteractionResult.PASS
        }

        UseBlockCallback.EVENT.register { player, level, hand, result ->
            val blockPos = result.blockPos
            val grave = Database.getGraveAt(player.uuid, blockPos) ?: return@register InteractionResult.PASS
            Database.removeGrave(grave)

            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3)

            if (level is ServerLevel) {
                level.sendParticles(
                    ParticleTypes.CLOUD,
                    blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5,
                    15,
                    0.2, 0.2, 0.2,
                    0.05
                )

                level.sendParticles(
                    ParticleTypes.SOUL,
                    blockPos.x + 0.5, blockPos.y + 0.2, blockPos.z + 0.5,
                    5, 0.1, 0.5, 0.1, 0.02
                )
            }

            level.playSound(null, blockPos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, 1.2f)
            level.playSound(null, blockPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 0.8f)
            level.playSound(null, blockPos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f)

            grave.items.forEach { stack ->
                if (!player.inventory.add(stack)) {
                    player.drop(stack, false, Prediction.PREDICTED)
                }
            }

            player.swing()
            EwConnect.discordBot?.onGraveReclaim((player as ServerPlayer).formattedDiscordNickname, player.uuid)

            return@register InteractionResult.SUCCESS_SERVER
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            FabricScheduler.tick(server)
        }

        UseEntityCallback.EVENT.register { player, level, hand, entity, result ->

            if (level.isClientSide) return@register InteractionResult.PASS

            if (player !is ServerPlayer) return@register InteractionResult.PASS

            activeEvents.forEach { event ->
                if (event is BirthdayEvent) {
                    event.onEntityInteraction(player, level as ServerLevel, hand, entity)
                }
            }

            if (player is ServerPlayer && entity is ServerPlayer) {


                // how???
                if (player == entity || player.isPassengerOfSameVehicle(entity)) {
                    return@register InteractionResult.PASS
                }

                var top = entity
                while (top.passengers.isNotEmpty()) {
                    top = top.passengers.last()
                }

                val success = SharedHandler.rideEntity(player, top, level, hand)

                level.playSound(
                    null,
                    top.x, top.y, top.z,
                    SoundEvents.HORSE_SADDLE,
                    SoundSource.PLAYERS,
                    1.0f,
                    1.2f
                )

                level.addParticle(
                    ParticleTypes.POOF,
                    false,
                    false,
                    top.x, top.y + 1.0, top.z,
                    0.3,
                    0.2,
                    0.3,
                )
                return@register InteractionResult.CONSUME
            }

            return@register InteractionResult.PASS
        }

        PlayerBlockBreakEvents.AFTER.register { level, player, pos, state, entity ->
            
            if(player.getItemHeldByArm(player.mainArm).enchantments.keySet().any { i -> i.`is`(Enchantments.SILK_TOUCH) }) {
                return@register
            }
            
            activeEvents.forEach { event ->
                if(event is MiningFrenzy) {
                    event.onBlockMined(state.block, player as ServerPlayer)
                }
            }
        }
        
    }

    companion object {

        private const val MOTD: String = "<#fcdbff>the <#f475ff>ew nation <#fcdbff>Minecraft survival server!"
        private val TRANSLATED_MOTD: Component = ChatUtils.translated(MOTD).toNMSComponent()

        @JvmStatic
        fun onChatMessageCallback(player: ServerPlayer, message: String) {
            EwConnect.discordBot?.onPlayerChat(player.formattedDiscordNickname, message)
            ChatUtils.sendPlayerChatMessage(player, message)
        }

        @JvmStatic
        fun onAdvancementCallback(player: ServerPlayer, advancement: AdvancementHolder) {
            val info = Advancement.name(advancement).string
            if (advancement.value.display.isPresent) {
                EwConnect.discordBot?.onAdvancement(
                    player.plainTextName,
                    player.uuid,
                    info,
                    advancement.value.display.get().description.string
                )
            }
        }

        @JvmStatic
        fun onServerStatusPingCallback(connection: Connection, oldStatus: ServerStatus) {
            val motd = EwConnect.activeEvents.lastOrNull { e -> e.overridesMotd != null }?.overridesMotd?.translated()
                ?.toNMSComponent() ?: TRANSLATED_MOTD

            val newStatus = ServerStatus(
                motd,
                oldStatus.players,
                oldStatus.version,
                oldStatus.favicon,
                oldStatus.enforcesSecureChat()
            )
            connection.send(ClientboundStatusResponsePacket(newStatus))
        }
    }

}