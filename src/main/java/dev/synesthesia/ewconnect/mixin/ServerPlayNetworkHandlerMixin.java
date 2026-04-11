package dev.synesthesia.ewconnect.mixin;

import dev.synesthesia.ewconnect.EventHandlers;
import dev.synesthesia.ewconnect.EwConnect;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
    private void onMessage(ServerboundChatPacket serverboundChatPacket, CallbackInfo ci) {
        var message = serverboundChatPacket.message();
        EventHandlers.onChatMessageCallback(player, message);
        ci.cancel();
    }


}
