package dev.synesthesia.ewconnect.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.synesthesia.ewconnect.utils.SharedHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "removePassenger", at = @At("TAIL"))
    private void removePassenger(Entity passenger, CallbackInfo ci) {
        SharedHandler.INSTANCE.onDismount((Entity) (Object) this);
    }

    @Inject(method = "addPassenger", at = @At("TAIL"))
    private void onAddPassenger(Entity passenger, CallbackInfo ci) {
        SharedHandler.INSTANCE.onMount((Entity) (Object) this, passenger);
    }

    @WrapOperation(
            method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z")
    )
    private boolean playerladder$allowRidingPlayers(EntityType instance, Operation<Boolean> original) {
        if(instance == EntityTypes.PLAYER) {
            return true;
        }else{
            return original.call(instance);
        }
    }
}
