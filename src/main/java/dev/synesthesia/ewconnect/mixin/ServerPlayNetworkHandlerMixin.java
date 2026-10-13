package dev.synesthesia.ewconnect.mixin;

import dev.synesthesia.ewconnect.ChatUtils;
import dev.synesthesia.ewconnect.EventHandlers;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(at = @At("HEAD"), method = "handleChat", cancellable = true)
    private void onMessage(ServerboundChatPacket packet, CallbackInfo ci) {
        var message = packet.message();
        EventHandlers.onChatMessageCallback(player, message);
        ci.cancel();
    }

//    @Inject(at = @At("HEAD"), method = "handleInteract", cancellable = true)
//    public void onHandleInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
//        ServerLevel level = this.player.level();
//        var entity = level.getEntity(packet.entityId());
//        
//        if (entity instanceof Allay allay && allay.entityTags().contains("ewconnect_ghost")) {
//            ChatUtils.sendMessage("cancelled");
//            ci.cancel();
//        }
//    }

}
