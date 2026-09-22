package dev.synesthesia.ewconnect.event.bonfire

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.event.IServerEvent
import dev.synesthesia.ewconnect.extensions.giveOrDrop
import dev.synesthesia.ewconnect.extensions.playSound
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.extensions.toNMSComponent
import dev.synesthesia.ewconnect.utils.FabricScheduler
import dev.synesthesia.ewconnect.utils.Location
import dev.synesthesia.ewconnect.utils.item
import net.kyori.adventure.bossbar.BossBar
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.core.component.DataComponentExactPredicate
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.zombie.Zombie
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.geysermc.floodgate.api.FloodgateApi
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import kotlin.math.min
import kotlin.random.Random

class BonfireFestivalEvent : IServerEvent {

    override val title: String = "Bonfire Festival"
    override val overridesMotd: String =
        "<#f475ff>★ <#eb008d><bold>EW NATION</bold> <#f475ff>★\n<#a3a3a3>Come and join the <#ff8d63>Bonfire Festival<#a3a3a3> :3"

    companion object {

        const val HOLOGRAM_KEY = "bonfire_festival_hologram"
        
        const val EMBERS_REQUIRED = 3

        val HOLOGRAM_POSITION = Location(5.5, 66.5, -26.5, "minecraft:overworld")
        val BONFIRE_POSITION = Location(11, 68, -23, "minecraft:overworld")

        val FLAMES: List<Flame> = listOf(
            Flame(BlockPos(-27, 71, 12)),
            Flame(BlockPos(179, 73, 84)),
            Flame(BlockPos(-24, 71, 107)),
            Flame(BlockPos(157, 73, -47)),
            Flame(BlockPos(77, 85, 29)),
            Flame(BlockPos(-85, 136, 70)),
            Flame(BlockPos(56, 129, -122)),
            Flame(BlockPos(-125, 70, 314)),
            Flame(BlockPos(347, 65, 173)),
            Flame(BlockPos(-164, 137, -128)),
            Flame(BlockPos(158, 70, 308))
        )

        val EMBERS_ITEM_STACK
            get() = item(Items.BLAZE_POWDER, 1) {
                name("<#f56c42><bold>Bonfire Ember")
                lore(
                    "",
                    "<gray>Deliver to the <yellow>central",
                    "<yellow>bonfire <gray>at spawn!",
                    "",
                    "<dark_gray>[Event Item]"
                )
                shiny()
            }

        val FESTIVAL_TICKET_ITEM_STACK
            get() = item(Items.DYE.orange(), 1) {
                name("<yellow><bold>Festival Ticket")
                lore(
                    "",
                    "<gray>Currency you can purchase stuff",
                    "<gray>with at the <light_purple>Festival Merchant",
                    "",
                    "<dark_gray>[Event Item]"
                )
                shiny()
            }

        fun festivalTicketCost(cost: Int) = ItemCost(
            FESTIVAL_TICKET_ITEM_STACK.item.builtInRegistryHolder(),
            cost,
            DataComponentExactPredicate.allOf(FESTIVAL_TICKET_ITEM_STACK.components)
        )

        val FLAME_SEEKER
            get() = item(Items.ENDER_EYE, 1) {
                name("<#f56c42><bold>Flame Seeker")
                lore(
                    "",
                    "<gray>Points in the direction of <#ff8d63>today's",
                    "<#ff8d63>${EMBERS_REQUIRED} flames<gray>!",
                    "",
                    "<yellow>[Throw to use]"
                )
                shiny()
            }

        fun getAssignedToday(collectedRecently: MutableList<Int>): List<Flame> {
            var candidates = FLAMES.filter { it.id() !in collectedRecently }

            if (candidates.size < EMBERS_REQUIRED) {
                collectedRecently.clear()
                candidates = FLAMES
            }

            val assigned = candidates.shuffled().take(EMBERS_REQUIRED)

            assigned.forEach { collectedRecently.add(it.id()) }

            if (collectedRecently.size > 10) {
                repeat(assigned.size) { collectedRecently.removeAt(0) }
            }

            return assigned
        }
    }

    override fun onInit(server: MinecraftServer) {

        val hologramManager = EwConnect.hologramManager
        hologramManager.remove(HOLOGRAM_KEY)
        val world = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse("minecraft:overworld")))!!

        hologramManager.create(HOLOGRAM_KEY) {
            setLocation(HOLOGRAM_POSITION)

            addStatic("<#f56c42>\uD83D\uDD25 <bold>Bonfire Festival</bold> \uD83D\uDD25")
            addStatic("")
            addStatic("<white>Every day, <yellow>${EMBERS_REQUIRED} flames<white> will be chosen.")
            addStatic("<white>Bring their <#ff8d63>Embers<white> back here")
            addStatic("<white>and drop them into the <#f56c42>bonfire<white>")
            addStatic("<white>to receive rewards!")
            addStatic("")
            addStatic("<white>Find flames by right clicking with")
            addStatic("<white>the <#ff8d63>Flame Seeker<white>!")
            addStatic("You can obtain it using <yellow>/flameseeker")

            FLAMES.forEach { flame ->
                val current = world.getBlockState(flame.position)
                world.setBlock(flame.position, current.setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL)
            }
        }

        FabricScheduler.runRepeating(0, 1) {

            val radius = 2
            val searchBox = BoundingBox(
                BONFIRE_POSITION.x.toInt() - radius,
                BONFIRE_POSITION.y.toInt() - radius,
                BONFIRE_POSITION.z.toInt() - radius,
                BONFIRE_POSITION.x.toInt() + radius,
                BONFIRE_POSITION.y.toInt() + radius,
                BONFIRE_POSITION.z.toInt() + radius
            )

            val dropEntities = world.getEntitiesOfClass(ItemEntity::class.java, AABB.of(searchBox))
                .filter { entity ->
                    ItemStack.isSameItemSameComponents(entity.item, EMBERS_ITEM_STACK) &&
                            entity.position().distanceTo(BONFIRE_POSITION.toVec3()) <= 2.0
                }

            dropEntities.forEach { item ->
                val player = item.owner ?: return@forEach
                if (player !is ServerPlayer) return@forEach
                var acceptedEmbers = false

                val amount = item.item.count

                Database.editBonfirePlayerData(player.uuid) { data ->

                    if (!data.isAvailableToday) {
                        return@editBonfirePlayerData
                    }

                    data.bankedEmbers += amount
                    acceptedEmbers = true

                    player.playSound(SoundEvents.FIRECHARGE_USE, 0.8f, 0f, SoundSource.MASTER)
                    player.playSound(SoundEvents.TOTEM_USE, 0.3f, 2f, SoundSource.MASTER)

                    player.level().sendParticles(
                        ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER,
                        BONFIRE_POSITION.x + 0.5, BONFIRE_POSITION.y + 0.5, BONFIRE_POSITION.z + 0.5,
                        50,
                        2.5, 2.5, 2.5,
                        0.01
                    )

                    player.level().sendParticles(
                        ParticleTypes.LAVA,
                        BONFIRE_POSITION.x + 0.5, BONFIRE_POSITION.y + 0.5, BONFIRE_POSITION.z + 0.5,
                        50,
                        2.5, 2.5, 2.5,
                        0.01
                    )

                    player.send("<#f56c42>\uD83D\uDD25 <grey>Ember accepted! <yellow>(${data.bankedEmbers}/${EMBERS_REQUIRED})")

                    if (data.bankedEmbers >= EMBERS_REQUIRED) {
                        data.lastCompletionTime = System.currentTimeMillis()
                        data.totalDaysCompleted += 1
                        data.bankedEmbers = 0

                        player.playSound(SoundEvents.VAULT_ACTIVATE, 1.3f, 1f, SoundSource.MASTER)
                        player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.5f, 0f, SoundSource.MASTER)
                        player.playSound(SoundEvents.END_PORTAL_SPAWN, 0.5f, 2f, SoundSource.MASTER)

                        player.send("<#f56c42>\uD83D\uDD25 <green>You have found and delivered today's embers! Here are thy rewards..")

                        FabricScheduler.repeatWithDelay(3, 5) {
                            player.giveOrDrop(FESTIVAL_TICKET_ITEM_STACK)
                            player.playSound(SoundEvents.ITEM_PICKUP, 2f, 1f, SoundSource.MASTER)
                        }
                        FabricScheduler.runLater(4 * 5) {
                            player.send("<#f56c42>\uD83D\uDD25 <white>[+3 Festival Tickets]")
                            player.playSound(SoundEvents.PLAYER_LEVELUP, 1f, 2f, SoundSource.MASTER)
                        }
                    }
                }

                val count = item.item.count
                item.discard()
                if (!acceptedEmbers) {
                    player.giveOrDrop(EMBERS_ITEM_STACK.copyWithCount(count))
                    player.send("<#f56c42>\uD83D\uDD25 <red>You have been refunded <yellow>${count}x Bonfire Ember(s)<red>..")
                }
            }

            return@runRepeating true
        }

        FabricScheduler.runRepeating(0, 10) {
            val world = BONFIRE_POSITION.serverLevel
            val blockPos = BONFIRE_POSITION.toBlockPos()
            world.playSound(
                null,
                blockPos,
                SoundEvents.FIRE_AMBIENT,
                SoundSource.AMBIENT,
                0.5f,
                (Random.nextFloat() + 0.5f).coerceIn(0.5f, 2f)
            )

            world.sendParticles(
                ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER,
                blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5,
                20,
                2.5, 2.5, 2.5,
                0.01
            )

            return@runRepeating true
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    override fun onPlayerJoin(player: ServerPlayer) {
        Database.editBonfirePlayerData(player.uuid) { data ->

            val currentTime = System.currentTimeMillis()
            val hasAssignmentExpired = (currentTime - data.lastAssignmentTime) >= BonfirePlayerData.ONE_DAY_MS

            if (data.isAvailableToday) {

                if (hasAssignmentExpired) {
                    data.assignedToday =
                        getAssignedToday(data.flamesCollectedRecently).shuffled().take(EMBERS_REQUIRED).map { flame -> flame.id() }
                            .toMutableList()

                    data.lastAssignmentTime = currentTime
                    data.bankedEmbers = 0
                }

                if (data.assignedToday.isNotEmpty()) {
                    player.send("<#f56c42>\uD83D\uDD25 <yellow>Use your <#ff8d63>Flame Seeker<yellow> to find today's flames!")
                    player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 2f, 0f, SoundSource.MASTER)
                } else {
                    player.send("<#f56c42>\uD83D\uDD25 <yellow>You have embers! Deliver them to the <#f56c42>central bonfire<yellow>!")
                    player.playSound(SoundEvents.PLAYER_LEVELUP, 2f, 2f, SoundSource.MASTER)
                }
            }
        }

        if(FloodgateApi.getInstance().isFloodgatePlayer(player.uuid)) return
        
        val bossbar: BossBar = BossBar.bossBar(
            ChatUtils.translated("<#f56c42><bold>Bonfire Festival!</bold> <#ff8d63>Use <white>/festival <#ff8d63>to see more"),
            1f,
            BossBar.Color.RED,
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
    }

    fun onRightClickWithItem(player: ServerPlayer, item: ItemStack): Boolean {
        if (!ItemStack.isSameItemSameComponents(item, FLAME_SEEKER)) return false

        val data = Database.getBonfirePlayerData(player.uuid)

        if (!data.isAvailableToday) {
            player.send("<#f56c42>\uD83D\uDD25 <red>You have no more flames to seek today!")
            player.playSound(SoundEvents.BLAZE_HURT, 1f, 1f, SoundSource.MASTER)
            return true
        }

        val targetFlame = data.assignedToday
            .map { id -> FLAMES.firstOrNull { it.id() == id } }
            .firstOrNull()

        if (targetFlame == null) {
            player.send("<#f56c42>\uD83D\uDD25 <red>You have no more flames to seek today!")
            player.playSound(SoundEvents.BLAZE_HURT, 1f, 1f, SoundSource.MASTER)
            return true
        }

        val world = player.level()
        val start = player.position().add(0.0, 1.2, 0.0)
        val end = Vec3.atCenterOf(targetFlame.position)

        val fullDistance = start.distanceTo(end)
        val distance = min(fullDistance, 20.0)
        val direction = end.subtract(start).normalize()

        val step = 1.0
        val steps = (distance / step).toInt()

        player.playSound(SoundEvents.ENDER_EYE_LAUNCH, 0.6f, 1.2f, SoundSource.MASTER)
        player.playSound(SoundEvents.FIRECHARGE_USE, 0.6f, 1f, SoundSource.MASTER)
        player.playSound(SoundEvents.VAULT_ACTIVATE, 0.6f, 1f, SoundSource.MASTER)
        
        FabricScheduler.repeatWithDelay(times = steps, delayTicks = 1) { index ->
            val traveled = index * step
            val point = start.add(direction.scale(traveled))

            world.sendParticles(
                ParticleTypes.END_ROD,
                point.x, point.y, point.z,
                1,
                0.0, 0.0, 0.0,
                0.0
            )
            world.sendParticles(
                ParticleTypes.FLAME,
                point.x, point.y, point.z,
                1,
                0.0, 0.0, 0.0,
                0.0
            )
            world.sendParticles(
                ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER,
                point.x, point.y, point.z,
                1,
                0.0, 0.0, 0.0,
                0.0
            )

            player.playSound(SoundEvents.FIRECHARGE_USE, 0.1f, 2f, SoundSource.MASTER)
        }

        return true
    }

    fun onPlayerClickOnBlock(player: ServerPlayer, blockPos: BlockPos, world: ServerLevel) {

        val flame = FLAMES.firstOrNull { flame -> flame.position == blockPos }
        if (flame == null) return

        val data = Database.getBonfirePlayerData(player.uuid)

        if (!data.isAvailableToday) {
            player.send("<#f56c42>\uD83D\uDD25 <red>You have already collected two embers today..")
            player.playSound(SoundEvents.BLAZE_HURT, 1f, 1f, SoundSource.MASTER)
            player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.8f, 1f, SoundSource.MASTER)
            return
        }

        if (!data.assignedToday.contains(flame.id())) {
            player.send("<#f56c42>\uD83D\uDD25 <red>This is not the flame you need to collect ember from..")
            player.playSound(SoundEvents.BLAZE_HURT, 1f, 1f, SoundSource.MASTER)
            player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.8f, 1f, SoundSource.MASTER)
            return
        }

        Database.editBonfirePlayerData(player.uuid) { d ->
            d.flamesCollectedRecently.add(flame.id())
            d.assignedToday.remove(flame.id())
        }

        player.playSound(SoundEvents.FIRECHARGE_USE, 0.8f, 0f, SoundSource.MASTER)
        player.playSound(SoundEvents.VAULT_ACTIVATE, 1.3f, 1f, SoundSource.MASTER)
        player.playSound(SoundEvents.TOTEM_USE, 0.3f, 2f, SoundSource.MASTER)
        player.playSound(SoundEvents.ITEM_PICKUP, 1f, 1f, SoundSource.MASTER)
        player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.5f, 0f, SoundSource.MASTER)

        spawnEmberGuardians(world, blockPos, player)
        
        player.level().sendParticles(
            ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER,
            blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5,
            50,
            1.0, 1.0, 1.0,
            0.01
        )

        player.level().sendParticles(
            ParticleTypes.LAVA,
            blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5,
            25,
            1.0, 1.0, 1.0,
            0.01
        )

        player.send("<#f56c42>\uD83D\uDD25 <gray>Collected a <yellow>Flame Ember<gray>, deliver it to the main bonfire!")
        player.giveOrDrop(EMBERS_ITEM_STACK)
    }

    override fun registerCommands(commandManager: FabricServerCommandManager<CommandSourceStack>) {
        BonfireCommands(commandManager)

        commandManager.buildAndRegister("flameseeker") {
            handler { context ->
                val player = context.sender().player ?: return@handler

                if (player.inventory.contains(FLAME_SEEKER)) {
                    player.send("<#f56c42>\uD83D\uDD25 <red>You already have a flame seeker..")
                    player.playSound(SoundEvents.BLAZE_HURT, 1f, 1f, SoundSource.MASTER)
                    return@handler
                }

                player.giveOrDrop(FLAME_SEEKER)
                player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.8f, 0f, SoundSource.MASTER)
                player.playSound(SoundEvents.ITEM_PICKUP, 1f, 1f, SoundSource.MASTER)
                player.playSound(SoundEvents.WITHER_SHOOT, 0.5f, 0f, SoundSource.MASTER)
                player.playSound(SoundEvents.ENDER_EYE_DEATH, 0.5f, 1f, SoundSource.MASTER)
            }
        }
    }

    private fun spawnEmberGuardians(
        world: ServerLevel,
        center: BlockPos,
        targetPlayer: ServerPlayer,
        count: Int = Random.nextInt(2, 4)
    ) {
        repeat(count) {
            val guardian = Zombie(EntityTypes.ZOMBIE, world)
            world.addFreshEntity(guardian)

            val offsetX = Random.nextDouble(-2.5, 2.5)
            val offsetZ = Random.nextDouble(-2.5, 2.5)
            val spawnX = center.x + 0.5 + offsetX
            val spawnY = center.y + 1.0
            val spawnZ = center.z + 0.5 + offsetZ

            guardian.setPos(spawnX, spawnY, spawnZ)

            guardian.customName = ChatUtils.translated("<#f56c42>Ember Guardian").toNMSComponent()
            guardian.isCustomNameVisible = true

            applyRandomEmberGear(guardian)

            guardian.target = targetPlayer

            world.addFreshEntity(guardian)
        }
    }

    private fun applyRandomEmberGear(guardian: Zombie) {
        val helmets = listOf(Items.MAGMA_BLOCK)
        val chestplates = listOf(Items.GOLDEN_CHESTPLATE, Items.COPPER_CHESTPLATE, Items.LEATHER_CHESTPLATE, Items.AIR)
        val leggings = listOf(Items.COPPER_LEGGINGS, Items.AIR)
        val boots = listOf(Items.GOLDEN_BOOTS, Items.COPPER_CHESTPLATE, Items.AIR)

        val weapons = listOf(
            Items.BLAZE_ROD,
            Items.GOLDEN_SWORD,
            Items.IRON_SWORD,
            Items.STONE_AXE,
            Items.STONE_SWORD,
            Items.COPPER_SWORD,
            Items.COPPER_AXE,
        )

        fun equipSlot(slot: EquipmentSlot, pool: List<Item>) {
            val item = pool.random()
            if (item != Items.AIR) {
                guardian .setItemSlot(slot, ItemStack(item))
            }
            guardian.setDropChance(slot, 0.0f)
        }

        equipSlot(EquipmentSlot.MAINHAND, weapons)
        equipSlot(EquipmentSlot.HEAD, helmets)
        equipSlot(EquipmentSlot.CHEST, chestplates)
        equipSlot(EquipmentSlot.LEGS, leggings)
        equipSlot(EquipmentSlot.FEET, boots)

    }

}