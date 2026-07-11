package dev.synesthesia.ewconnect.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public interface PlayerListAccessor {
    
    @Accessor("stats")
    Map<UUID, ServerStatsCounter> getStatsMap();

    @Invoker("locateStatsFile")
    Path getLocateStatsFile(final GameProfile gameProfile);
}
