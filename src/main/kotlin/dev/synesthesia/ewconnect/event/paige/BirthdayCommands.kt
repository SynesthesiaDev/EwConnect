package dev.synesthesia.ewconnect.event.paige

import com.mojang.math.Transformation
import dev.synesthesia.ewconnect.extensions.location
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.level.block.Blocks
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f


class BirthdayCommands(manager: FabricServerCommandManager<CommandSourceStack>) {

    private fun getSuggestions(): BlockingSuggestionProvider.Strings<CommandSourceStack> {
        return BlockingSuggestionProvider.Strings { _, _ -> BirthdayNpcs.MESSAGES.keys }
    }

    init {
        val base = manager.commandBuilder("birthday")

        manager.command(
            base.literal("npc")
                .permission("ew.admin")
                .required("name", stringParser(), getSuggestions())
                .handler { context ->
                    var player = context.sender().player!!
                    var name = context.get<String>("name")
                    
                    BirthdayNpcs.spawnNpc(player.location, BirthdayNpcs.MESSAGES[name]!!, Vector2f(player.yRot, 0f))
                }
        )

        manager.buildAndRegister("spawn_cake") {
            permission("ew.admin")
            handler { context ->
                val bottomLocation = context.sender().player!!.location.subtract(1.0, 0.0, 1.0)
                var bottomCake = Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, bottomLocation.serverLevel)

                var topCake = Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, bottomLocation.serverLevel)
                val topLocation = context.sender().player!!.location.subtract(0.5, -0.95, 0.5)

                val bottomTrans = Transformation(
                    Vector3f(0.0f, 0.0f, 0.0f),
                    Quaternionf(),
                    Vector3f(2.0f, 2.0f, 2.0f),
                    Quaternionf()
                )

                val topTrans = Transformation(
                    Vector3f(0.0f, 0.0f, 0.0f),
                    Quaternionf(),
                    Vector3f(1.0f, 1.0f, 1.0f),
                    Quaternionf()
                )

                bottomCake.blockState = Blocks.CAKE.defaultBlockState()
                bottomCake.setTransformation(bottomTrans)
                bottomCake.teleportTo(bottomLocation.x, bottomLocation.y, bottomLocation.z);

                topCake.blockState = Blocks.CANDLE_CAKE.defaultBlockState()
                topCake.setTransformation(topTrans)
                topCake.teleportTo(topLocation.x, topLocation.y, topLocation.z);

                bottomLocation.serverLevel.addFreshEntity(bottomCake);
                bottomLocation.serverLevel.addFreshEntity(topCake);
            }
        }
    }
}