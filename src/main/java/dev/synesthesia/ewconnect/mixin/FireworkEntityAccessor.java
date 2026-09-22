package dev.synesthesia.ewconnect.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(FireworkRocketEntity.class)
public interface FireworkEntityAccessor {
    
    @Invoker("explode")
    void invokeExplode(final ServerLevel level);
    
}
