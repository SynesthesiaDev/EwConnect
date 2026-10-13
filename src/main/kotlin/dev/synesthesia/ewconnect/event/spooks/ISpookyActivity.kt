package dev.synesthesia.ewconnect.event.spooks

import net.minecraft.server.level.ServerPlayer

interface ISpookyActivity {
    
    fun canEnact(player: ServerPlayer)
    fun enact(player: ServerPlayer)
    
}