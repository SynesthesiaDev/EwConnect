package dev.synesthesia.ewconnect.utils

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.extensions.toNMSComponent
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

fun item(item: Item, count: Int = 1, block: ItemBuilder.() -> Unit): ItemStack {
    val builder = ItemBuilder(ItemStack(item, count))
    builder.block()
    return builder.build()
}

class ItemBuilder(private val stack: ItemStack) {

    fun name(text: String) {
        stack.set(DataComponents.CUSTOM_NAME, ChatUtils.translated(text).toNMSComponent())
    }

    fun lore(vararg line: String) {
        val lines = mutableListOf<Component>()
        line.forEach { l -> lines.add(ChatUtils.translated(l).toNMSComponent()) }
        stack.set(DataComponents.LORE, ItemLore(lines))
    }

    fun shiny() {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    }
    
    fun build(): ItemStack = stack
}