package dev.synesthesia.ewconnect.mixin;

import dev.synesthesia.ewconnect.EventHandlers;
import dev.synesthesia.ewconnect.EwConnect;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerStatusPacketListenerImpl.class)
public class ServerStatusPacketListenerMixin {

    @Shadow
    @Final
    private ServerStatus status;

    @Shadow
    private boolean hasRequestedStatus;

    @Shadow
    @Final
    private Connection connection;

    @Shadow
    @Final
    private static Component DISCONNECT_REASON;

    @Inject(at = @At("HEAD"), method = "handleStatusRequest", cancellable = true)
    private void handleStatusRequest(ServerboundStatusRequestPacket serverboundStatusRequestPacket, CallbackInfo ci) {
        if (this.hasRequestedStatus) {
            this.connection.disconnect(DISCONNECT_REASON);
        } else {
            this.hasRequestedStatus = true;
            EventHandlers.onServerStatusPingCallback(connection, status);
        }
        ci.cancel();
    }

}
