package dev.synesthesia.ewconnect.items

import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.database.Database
import dev.synesthesia.ewconnect.extensions.formattedDiscordNickname
import dev.synesthesia.ewconnect.extensions.giveOrDrop
import dev.synesthesia.ewconnect.extensions.location
import dev.synesthesia.ewconnect.extensions.playSound
import dev.synesthesia.ewconnect.extensions.send
import dev.synesthesia.ewconnect.utils.FabricScheduler
import dev.synesthesia.ewconnect.utils.Location
import dev.synesthesia.ewconnect.utils.itemBuilder
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks

class RecoveryEcho : ICustomItem {

    override val identifier: String = "recovery_echo"

    companion object {
        val ITEM_BUILDER = itemBuilder(Items.ECHO_SHARD) {
            shiny()
            name("<#add9ff><bold>Recovery Echo")
            lore(
                "",
                "<gray>Use <yellow>Right-Click <gray>to instantly",
                "<gray>recover all of your graves",
                ""
            )
        }
    }
    
    override val itemStack: ItemStack get() = ITEM_BUILDER.build()
    val itemStackTemplate: ItemStackTemplate get() = ITEM_BUILDER.buildTemplate()

    override fun onRightClick(player: ServerPlayer) {

        val graves = Database.getGraves(player.uuid)
        if (graves.isEmpty()) {
            player.send("<red>(!) You have no graves")
            return
        }

        player.inventory.forEach { item ->
            if (ItemStack.isSameItemSameComponents(item, itemStack)) {
                player.inventory.removeItem(item)
                return@forEach
            }
        }

        player.playSound(SoundEvents.ENDER_EYE_DEATH, 2f, 0f, SoundSource.MASTER)
        player.playSound(SoundEvents.END_PORTAL_SPAWN, 0.1f, 0f, SoundSource.MASTER)

        val blockPos = player.location

        FabricScheduler.repeatWithDelay(10, 2) {
            player.playSound(SoundEvents.PHANTOM_AMBIENT, 2f, 0f, SoundSource.MASTER)

            player.level().sendParticles(
                ParticleTypes.SCULK_SOUL,
                blockPos.x, blockPos.y, blockPos.z,
                15, 1.0, 1.0, 1.0, 0.05
            )

            player.level().sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                blockPos.x, blockPos.y, blockPos.z,
                15, 0.5, 0.5, 0.5, 1.0
            )
        }

        graves.forEach { grave ->
            Database.removeGrave(grave)

            val pos = grave.location
            val location = Location(pos.x, pos.y, pos.z, grave.world)

            location.serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)
            grave.items.forEach { stack ->
                player.giveOrDrop(stack)
            }

            EwConnect.discordBot?.onGraveReclaim(player.formattedDiscordNickname, player.uuid)
        }

    }

}