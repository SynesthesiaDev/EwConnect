package dev.synesthesia.ewconnect.mixin;

import dev.synesthesia.ewconnect.ChatUtils;
import dev.synesthesia.ewconnect.event.bonfire.FestivalItems;
import dev.synesthesia.ewconnect.event.paige.MiningFrenzyItems;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Snowball.class)
public abstract class SnowballMixin {

    @Shadow
    protected abstract Item getDefaultItem();

    @Inject(at = @At("HEAD"), method = "onHit")
    protected void onHit(HitResult hitResult, CallbackInfo ci) {
        var accessor = (ThrowableItemProjectileAccessor)(Object) this;
        var item = accessor.getItemInvoker();

        var pos = hitResult.getLocation();
        var world = ((Entity)(Object)this).level();

        if(ItemStack.isSameItemSameComponents(item, FestivalItems.FIRECRACKERS)) {

            Fireworks fireworks = getFireworks(0xFF6A00, 0xFFFF00);

            ItemStack rocketStack = new ItemStack(Items.FIREWORK_ROCKET);
            rocketStack.set(DataComponents.FIREWORKS, fireworks);

            FireworkRocketEntity firework = new FireworkRocketEntity(
                    world, pos.x, pos.y, pos.z, rocketStack
            );

            world.addFreshEntity(firework);
            ((FireworkEntityAccessor)firework).invokeExplode((ServerLevel) world);
        }

        if(ItemStack.isSameItemSameComponents(item, MiningFrenzyItems.FIRECRACKERS)) {
            Fireworks fireworks = getFireworks(0xFC0390, 0x7303FC);

            ItemStack rocketStack = new ItemStack(Items.FIREWORK_ROCKET);
            rocketStack.set(DataComponents.FIREWORKS, fireworks);

            FireworkRocketEntity firework = new FireworkRocketEntity(
                    world, pos.x, pos.y, pos.z, rocketStack
            );

            world.addFreshEntity(firework);
            ((FireworkEntityAccessor)firework).invokeExplode((ServerLevel) world);
        }
    }

    private static @NonNull Fireworks getFireworks(int e1, int e2) {
        IntList colors = new IntArrayList(List.of(e1, e2));
        IntList fadeColors = new IntArrayList(List.of());

        FireworkExplosion explosion = new FireworkExplosion(
                FireworkExplosion.Shape.BURST,
                colors,
                fadeColors,
                false,
                true
        );

        Fireworks fireworks = new Fireworks(
                0,
                List.of(explosion, explosion)
        );
        return fireworks;
    }
}
