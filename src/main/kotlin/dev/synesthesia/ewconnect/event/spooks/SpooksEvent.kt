package dev.synesthesia.ewconnect.event.spooks

import dev.synesthesia.ewconnect.event.IServerEvent
import net.minecraft.server.level.ServerPlayer

class SpooksEvent() : IServerEvent {

    override val title: String = "Spooks"

    override fun onPlayerJoin(player: ServerPlayer) {
        
    }
    
    
}