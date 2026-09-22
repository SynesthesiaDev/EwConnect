package dev.synesthesia.ewconnect.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Redirect(
            method = "addEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;clientTrackingRange()I")
    )
    private int shrinkHologramRange(EntityType<?> type, Entity entity) {
        if (entity.entityTags().contains("ewconnect_cleanup_orphan")) {
            return 1;
        }
        
        if(entity.getType() == EntityTypes.ITEM_FRAME || entity.getType() == EntityTypes.GLOW_ITEM_FRAME) {
            return 2;
        }
        return type.clientTrackingRange();
    }
}
