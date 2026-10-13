package dev.synesthesia.ewconnect.mixin;

import dev.synesthesia.ewconnect.event.halloween.FishingMinigame;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Shadow
    private int nibble;

    @Inject(method = "tick", at = @At("TAIL"))
    private void ewconnect$onTick(CallbackInfo ci) {
        FishingHook self = (FishingHook) (Object) this;
        if (!(self.level() instanceof ServerLevel)) return;

        if (FishingMinigame.onHookTick(self, this.nibble > 0)) {
            this.nibble = Math.max(this.nibble, 5);
        }
    }

    @Inject(method = "retrieve", at = @At("HEAD"), cancellable = true)
    private void ewconnect$onRetrieve(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        FishingHook self = (FishingHook) (Object) this;
        
        if (!(self.level() instanceof ServerLevel)) return;

        if (FishingMinigame.onReel(self)) {
            cir.setReturnValue(1);
        }
    }
}