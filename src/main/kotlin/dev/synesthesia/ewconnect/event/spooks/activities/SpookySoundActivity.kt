package dev.synesthesia.ewconnect.event.spooks.activities

import dev.synesthesia.ewconnect.event.spooks.ISpookyActivity
import dev.synesthesia.ewconnect.extensions.sendPrivateSound
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents

class SpookySoundActivity : ISpookyActivity {
    
    val sounds = listOf(SoundEvents.BELL_RESONATE, SoundEvents.CREAKING_HEART_IDLE, SoundEvents.CREAKING_AMBIENT, SoundEvents.WARDEN_EMERGE)
    
    override fun canEnact(player: ServerPlayer) {
        
    }
    
    override fun enact(player: ServerPlayer) {
        player.sendPrivateSound(sounds.random(), 0f, 0f)
    }

}