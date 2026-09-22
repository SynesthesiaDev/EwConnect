package dev.synesthesia.ewconnect.mixin;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThrowableItemProjectile.class)
public interface ThrowableItemProjectileAccessor {
    
    @Invoker("getItem")
    ItemStack getItemInvoker();
    
}
