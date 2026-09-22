package dev.synesthesia.ewconnect.utils

import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment

object DungeonsAndTaverns {

    val OUTREACH: ResourceKey<Enchantment> = key("outreach")
    val ANTIDOTE: ResourceKey<Enchantment> = key("antidote")
    val SWIFT_SOAR: ResourceKey<Enchantment> = key("swift_soar")

    private fun key(id: String): ResourceKey<Enchantment> {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("nova_structures", id))
    }
}