package dev.synesthesia.ewconnect.utils

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.extensions.toNMSComponent
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments

fun itemBuilder(item: Item, count: Int = 1, block: ItemBuilder.() -> Unit): ItemBuilder {
    val builder = ItemBuilder(item, count)
    builder.block()
    return builder
}

fun item(item: Item, count: Int = 1, block: ItemBuilder.() -> Unit): ItemStack {
    val builder = ItemBuilder(item, count)
    builder.block()
    return builder.build()
}

fun itemTemplate(item: Item, block: ItemBuilder.() -> Unit): ItemStackTemplate {
    val builder = ItemBuilder(item, 1)
    builder.block()
    return builder.buildTemplate()
}

class ItemBuilder(private val item: Item, private val count: Int = 1) {

    private var nameComponent: Component? = null
    private var loreComponent: ItemLore? = null
    private var shinyFlag: Boolean? = null
    private var enchantmentsComponent: ItemEnchantments? = null

    fun name(text: String) {
        nameComponent = ChatUtils.translated(text).toNMSComponent()
    }

    fun lore(vararg line: String) {
        loreComponent = ItemLore(line.map { ChatUtils.translated(it).toNMSComponent() })
    }

    fun shiny() {
        shinyFlag = true
    }

    fun enchantments(vararg enchantments: Holder<Enchantment>, level: Int = 1) {
        val mutable = ItemEnchantments.Mutable(ItemEnchantments.EMPTY)
        enchantments.forEach { mutable.set(it, level) }
        enchantmentsComponent = mutable.toImmutable()
    }

    fun enchantments(vararg enchantments: Pair<ResourceKey<Enchantment>, Int>) {
        val provider = EwConnect.getRegistries()
        val lookup = provider.lookup(Registries.ENCHANTMENT).orElse(null) ?: return
        val mutable = ItemEnchantments.Mutable(ItemEnchantments.EMPTY)
        enchantments.forEach { (key, level) ->
            lookup.get(key).ifPresent { holder -> mutable.set(holder, level) }
        }
        enchantmentsComponent = mutable.toImmutable()
    }

    fun build(): ItemStack {
        val stack = ItemStack(item, count)
        nameComponent?.let { stack.set(DataComponents.CUSTOM_NAME, it) }
        loreComponent?.let { stack.set(DataComponents.LORE, it) }
        shinyFlag?.let { stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, it) }
        enchantmentsComponent?.let { stack.set(DataComponents.ENCHANTMENTS, it) }
        return stack
    }

    fun buildTemplate(): ItemStackTemplate {
        val patch = DataComponentPatch.builder()
        nameComponent?.let { patch.set(DataComponents.CUSTOM_NAME, it) }
        loreComponent?.let { patch.set(DataComponents.LORE, it) }
        shinyFlag?.let { patch.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, it) }
        enchantmentsComponent?.let { patch.set(DataComponents.ENCHANTMENTS, it) }
        return ItemStackTemplate(item, patch.build())
    }
}