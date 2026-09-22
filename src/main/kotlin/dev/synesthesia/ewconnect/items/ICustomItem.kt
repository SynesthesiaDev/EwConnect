package dev.synesthesia.ewconnect.items

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

interface ICustomItem {
    
    fun onRightClick(player: ServerPlayer)
    
    val itemStack: ItemStack
    
    val identifier: String
    
}