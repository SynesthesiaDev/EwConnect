package dev.synesthesia.ewconnect.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class EntityTypeMixin {
    
    @Inject(method = "updateInterval", at = @At("HEAD"), cancellable = true)
    private void throttleUpdates(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == EntityTypes.VILLAGER) {
            cir.setReturnValue(2 * 20);
        }

        if ((Object) this == EntityTypes.ARMOR_STAND) {
            cir.setReturnValue(5 * 20);
        }

        if ((Object) this == EntityTypes.EXPERIENCE_ORB) {
            cir.setReturnValue(10);
        }
    }
}
