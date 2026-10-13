package dev.synesthesia.ewconnect.event.halloween

import com.mojang.math.Transformation
import dev.synesthesia.ewconnect.entities.IFakeEntity
import dev.synesthesia.ewconnect.utils.FabricScheduler
import dev.synesthesia.ewconnect.utils.Location
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.allay.Allay
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import org.joml.Quaternionf
import org.joml.Vector3f

class SpoopyGhost : IFakeEntity {

    companion object {
        const val LIFETIME = 30 * 20
    }

    private var entity: Allay? = null
    private var itemDisplay: Display.ItemDisplay? = null
    private var aliveTicks: Int = 0

    fun spawn(location: Location): Entity {
        val level = location.serverLevel
        val allay = Allay(EntityTypes.ALLAY, level)

        level.addFreshEntity(allay)
        
        allay.teleportTo(location.x, location.y, location.z)
        allay.isInvisible = true
        allay.isPermanentlyInvulnerable = true
        allay.isSilent = true
        allay.getAttribute(Attributes.SCALE)?.baseValue = 0.00625
        
        val skull = Items.PLAYER_HEAD.defaultInstance.copy()
        skull.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved("SPIRITONPATROL"))
        
        itemDisplay = Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, level)
        level.addFreshEntity(itemDisplay!!)
        
        itemDisplay?.itemStack = skull
        itemDisplay?.posRotInterpolationDuration = 2
        itemDisplay?.setTransformation(
            Transformation(
                Vector3f(0f),
                Quaternionf().rotationY(Math.PI.toFloat()),
                Vector3f(1.25f),
                Quaternionf()
            )
        )
        
        allay.addTag("ewconnect_cleanup_orphan")
        allay.addTag("ewconnect_ghost")

        this.entity = allay

        FabricScheduler.runRepeating(0, 1) {
            tick()
            if (entity == null) {
                return@runRepeating false
            }
            
            return@runRepeating true
        }
        
        return allay
    }

    fun tick() {
        if (entity == null) {
            dispose()
            return
        }

        aliveTicks++
        val bat = entity!!

        (bat.level() as ServerLevel).sendParticles(
            ParticleTypes.SPIT,
            bat.x, bat.y, bat.z,
            1,
            0.0, 0.0, 0.0,
            0.0
        )
        
        itemDisplay?.teleportTo(bat.x, bat.y + 0.8, bat.z)

        val display = itemDisplay ?: return
        val player = (bat.level() as ServerLevel).getNearestPlayer(bat, 64.0)

        if (player != null) {
            val dx = player.x - display.x
            val dy = player.eyeY - display.y
            val dz = player.z - display.z
            val horizontal = kotlin.math.sqrt(dx * dx + dz * dz)

            val targetYaw = (Math.toDegrees(kotlin.math.atan2(dz, dx)) - 90.0).toFloat()
            val targetPitch = (-Math.toDegrees(kotlin.math.atan2(dy, horizontal))).toFloat()

            val smooth = 0.35f
            val yawDelta = ((targetYaw - display.yRot + 540f) % 360f) - 180f
            display.yRot += yawDelta * smooth
            display.xRot += (targetPitch - display.xRot) * smooth
        }
        
        if (aliveTicks == LIFETIME) {
            dispose()
        }
    }

    override fun dispose() {
        entity?.discard()
        itemDisplay?.discard()
        entity = null
    }
}