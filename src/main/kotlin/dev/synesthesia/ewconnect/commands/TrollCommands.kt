package dev.synesthesia.ewconnect.commands

import dev.synesthesia.ewconnect.EventHandlers
import dev.synesthesia.ewconnect.mixin.MannequinAccessor
import dev.synesthesia.ewconnect.utils.FabricScheduler
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.entity.decoration.Mannequin
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.phys.Vec3
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.minecraft.modded.data.MultiplePlayerSelector
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector
import org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers
import org.incendo.cloud.parser.standard.StringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import java.util.UUID

class TrollCommands(manager: FabricServerCommandManager<CommandSourceStack>) {

    init {
        manager.buildAndRegister("demoscreen") {
            required("player", VanillaArgumentParsers.singlePlayerSelectorParser())
            permission("ew.admin")
            handler { context ->
                val player = (context["player"] as SinglePlayerSelector).single()

                player.connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 0f))
            }
        }

        manager.buildAndRegister("winscreen") {
            required("player", VanillaArgumentParsers.singlePlayerSelectorParser())
            permission("ew.admin")
            handler { context ->
                val player = (context["player"] as SinglePlayerSelector).single()

                player.connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 1f))
            }
        }

        manager.buildAndRegister("mount") {
            required("target", VanillaArgumentParsers.singlePlayerSelectorParser())
            optional("player", VanillaArgumentParsers.singlePlayerSelectorParser())
            permission("ew.admin")
            handler { context ->
                val target = (context["target"] as SinglePlayerSelector).single()
                val player = (context.getOrNull("target") as SinglePlayerSelector?)?.single() ?: context.sender().player!!

                player.startRiding(target, true, true)
            }
        }

        manager.buildAndRegister("smite") {
            required("player", VanillaArgumentParsers.singlePlayerSelectorParser())
            permission("ew.admin")
            handler { context ->
                val player = (context["player"] as SinglePlayerSelector).single()

                var bolt = LightningBolt(EntityTypes.LIGHTNING_BOLT, player.level())
                var pos = player.position()
                bolt.teleportTo(pos.x, pos.y, pos.z)
                player.level().addFreshEntity(bolt)
            }
        }

        manager.buildAndRegister("sayas") {
            required("player", VanillaArgumentParsers.singlePlayerSelectorParser())
            required("text", stringParser(StringParser.StringMode.GREEDY))
            permission("ew.admin")
            handler { context ->
                val player = (context["player"] as SinglePlayerSelector).single()
                val text: String = context["text"]

                EventHandlers.onChatMessageCallback(player, text)
            }
        }

        manager.buildAndRegister("launch") {
            required("players", VanillaArgumentParsers.multiplePlayerSelectorParser())
            permission("ew.admin")
            handler { context ->
                val players = (context["players"] as MultiplePlayerSelector).values()
                players.forEach { player ->
                    smite(player, true)
                    player.setIgnoreFallDamageFromCurrentImpulse(true, Vec3.ZERO)
                    player.deltaMovement = player.deltaMovement.add(0.0, 5.0, 0.0)
                    player.setLastHurtByPlayer(player, 99999)
                }
            }
        }

        manager.buildAndRegister("herobrine") {
            required("player", VanillaArgumentParsers.singlePlayerSelectorParser())
            permission("ew.admin")
            handler { context ->
                val player = (context["player"] as SinglePlayerSelector).single()

                val level = player.level()
                val playerPos = player.position()

                val lookAngle = player.lookAngle
                val behindOffset = Vec3(-lookAngle.x, 0.0, -lookAngle.z).normalize().scale(2.5)
                val spawnPos = playerPos.add(behindOffset)

                var herobrine = Mannequin(EntityTypes.MANNEQUIN, player.level())
                var accessor = herobrine as MannequinAccessor;
                herobrine.teleportTo(level, spawnPos.x, player.y, spawnPos.z, emptySet(), player.yRot, 0f, false)
                
                val profile = ResolvableProfile.createUnresolved(UUID.fromString("72b1107f-31b9-43ae-91b6-19472f0c7cee"))
                accessor.invokeSetProfile(profile)

                player.level().addFreshEntity(herobrine)
                level.playSound(
                    null,
                    BlockPos(spawnPos.x.toInt(), spawnPos.y.toInt(), spawnPos.z.toInt()),
                    SoundEvents.AMBIENT_CAVE.value(),
                    SoundSource.MASTER,
                    2f,
                    2f
                )
                
                level.playSound(
                    null,
                    BlockPos(spawnPos.x.toInt(), spawnPos.y.toInt(), spawnPos.z.toInt()),
                    SoundEvents.GHAST_DEATH,
                    SoundSource.MASTER,
                    2f,
                    2f
                )
                
                var ticksPassed = 0
                FabricScheduler.runRepeating(0, 1) {

                    val playerEyePos = player.eyePosition
                    val toHerobrineVec = herobrine.position().subtract(playerEyePos).normalize()
                    val playerLookVec = player.lookAngle.normalize()

                    val dotProduct = playerLookVec.dot(toHerobrineVec)

                    ticksPassed++;
                    if (ticksPassed >= 10 * 20) {
                        return@runRepeating false
                    }

                    if (dotProduct > 0.8) {
                        level.playSound(
                            null,
                            BlockPos(spawnPos.x.toInt(), spawnPos.y.toInt(), spawnPos.z.toInt()),
                            SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.MASTER,
                            1.2f,
                            0.8f
                        )
                        
                        herobrine.discard()
                        return@runRepeating false
                    } else {
                        return@runRepeating true
                    }
                }
            }
        }
    }

    private fun smite(player: ServerPlayer, visualOnly: Boolean = false) {
        val bolt = LightningBolt(EntityTypes.LIGHTNING_BOLT, player.level())
        val pos = player.position()
        bolt.teleportTo(pos.x, pos.y, pos.z)
        bolt.setVisualOnly(visualOnly)
        player.level().addFreshEntity(bolt)
    }
}
