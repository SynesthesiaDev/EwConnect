package dev.synesthesia.ewconnect

import dev.synesthesia.ewconnect.commands.AdminGravesCommand
import dev.synesthesia.ewconnect.commands.AdminNicknameCommand
import dev.synesthesia.ewconnect.commands.ColorCommand
import dev.synesthesia.ewconnect.commands.GravesCommand
import dev.synesthesia.ewconnect.commands.NicknameCommand
import dev.synesthesia.ewconnect.database.serializers.ItemStackListDbSerializer
import dev.synesthesia.ewconnect.discord.DiscordBot
import net.fabricmc.api.ModInitializer
import net.minecraft.core.RegistryAccess
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.Item
import revxrsal.commands.Lamp
import revxrsal.commands.fabric.FabricLamp
import revxrsal.commands.fabric.actor.FabricCommandActor

class EwConnect : ModInitializer {

    companion object {
        lateinit var server: MinecraftServer
        lateinit var events: EventHandlers
        lateinit var lamp: Lamp<FabricCommandActor>
        var discordBot: DiscordBot? = null
        
    }

    override fun onInitialize() {
        events = EventHandlers(this)
        lamp = FabricLamp.builder().build()
        lamp.register(ColorCommand(), NicknameCommand(), AdminNicknameCommand(), AdminGravesCommand(), GravesCommand())
    }
}
