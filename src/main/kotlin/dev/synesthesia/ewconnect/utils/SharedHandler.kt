package dev.synesthesia.ewconnect.utils

import com.google.common.collect.Sets;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

object SharedHandler {

    fun rideEntity(player: Player, newVehicle: Entity, level: Level, hand: InteractionHand?): InteractionResult {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && canPickUpOrRideLiving(newVehicle) && player.getItemInHand(
                hand
            ).isEmpty
        ) {
            val vehicle: Entity =
                getHighestOrSelf(newVehicle, player, 255) ?: return InteractionResult.FAIL

            player.startRiding(vehicle)

            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    fun pickUpEntity(player: Player, newPassenger: Entity, level: Level, hand: InteractionHand?): InteractionResult {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && canPickUpOrRideLiving(newPassenger) && player.getItemInHand(
                hand
            ).isEmpty
        ) {
            val vehicle: Entity =
                getHighestOrSelf(player, newPassenger, 255) ?: return InteractionResult.FAIL

            newPassenger.startRiding(vehicle)

            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    private fun getHighestOrSelf(vehicle: Entity, newPassenger: Entity?, limit: Int): Entity? {
        var vehicle: Entity = vehicle
        var count = -1
        while (vehicle.isVehicle) {
            count++
            vehicle = vehicle.firstPassenger!!
            if (vehicle === newPassenger || count >= limit) return null
        }
        return vehicle
    }

    private fun canPickUpOrRideLiving(entity: Entity): Boolean {
        if (entity is Player) {
            return true
        }

        return false
    }

    fun onMount(vehicle: Entity, passenger: Entity?) {
        if (!vehicle.level().isClientSide && vehicle is Player) {
            (vehicle as ServerPlayer).connection.send(ClientboundSetPassengersPacket(vehicle))
        }
    }

    fun onDismount(vehicle: Entity) {
        if (!vehicle.level().isClientSide && vehicle is Player) (vehicle as ServerPlayer).connection.send(
            ClientboundSetPassengersPacket(vehicle)
        )
    }

    fun onPlayerTick(player: Player) {
//        if (!player.level().isClientSide && player.onGround() && player.isVehicle && player.isCrouching) player.firstPassenger?.stopRiding()
    }

    fun onLogOut(player: Player) {
        if (player.isPassenger && player.vehicle is Player) player.stopRiding()
    }

    fun onGameModeChange(player: Player) {
        if (player.isVehicle && (player.gameMode() === GameType.SPECTATOR)) player.firstPassenger?.stopRiding()
    }

}