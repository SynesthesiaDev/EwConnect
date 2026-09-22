package dev.synesthesia.ewconnect.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(PacketEncoder.class)
public class PacketEncoderMixin {

    @Unique
    private static final long THROTTLE_MS = 150;
    
    @Unique
    private static final Map<Integer, Long> LAST_SENT_MAP = new ConcurrentHashMap<>();

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V", at = @At("TAIL"), cancellable = true)
    private void encode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf output, CallbackInfo ci) {

        if (packet instanceof ClientboundMoveEntityPacket) {

            var currentTime = System.currentTimeMillis();

            var entityId = ((ClientboundMoveEntityPacketAccessor) packet).getEntityIdAccessor();
            long lastSent = LAST_SENT_MAP.getOrDefault(entityId, 0L);

            if (currentTime - lastSent < THROTTLE_MS) {
                ci.cancel();
            } else {
                LAST_SENT_MAP.put(entityId, currentTime);
            }
        }
    }
}
