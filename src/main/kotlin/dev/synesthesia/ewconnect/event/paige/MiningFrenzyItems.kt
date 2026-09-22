package dev.synesthesia.ewconnect.event.paige

import dev.synesthesia.ewconnect.event.bonfire.BonfireFestivalEvent
import dev.synesthesia.ewconnect.event.bonfire.BonfireFestivalEvent.Companion.FESTIVAL_TICKET_ITEM_STACK
import dev.synesthesia.ewconnect.event.bonfire.FestivalItems
import dev.synesthesia.ewconnect.event.bonfire.FestivalItems.DNT_ANTIDOTE
import dev.synesthesia.ewconnect.event.bonfire.FestivalItems.DNT_OUTREACH
import dev.synesthesia.ewconnect.event.bonfire.FestivalItems.DNT_SWIFT_SOAR
import dev.synesthesia.ewconnect.extensions.toNMSComponent
import dev.synesthesia.ewconnect.extensions.translated
import dev.synesthesia.ewconnect.utils.DungeonsAndTaverns
import dev.synesthesia.ewconnect.utils.Location
import dev.synesthesia.ewconnect.utils.item
import net.minecraft.core.component.DataComponentExactPredicate
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.trading.ItemCost
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers

object MiningFrenzyItems {

    @JvmField
    val FIRECRACKERS = item(Items.SNOWBALL, 4) {
        name("<#ffa1c8><bold>Birthday Firecrackers")
        lore(
            "",
            "<white>Right-click <gray>to spawn powerful pyrotechnics!",
            "<gray>Only slightly dangerous!",
            "",
            "<#ffa1c8>[Birthday Event Item]",
            ""
        )
        shiny()
    }
    
    @JvmField
    val SHARD_ITEM = item(Items.AMETHYST_SHARD) {
        name("<#c8a6ff><bold>Mining Shard")
        lore(
            "",
            "<gray>Mining shard that can be converted to",
            "<gray>the event shop currency",
            "",
            "<#c8a6ff>[Mining Frenzy Item]"
        )
        shiny()
    }

    @JvmField
    val GEODE_ITEM = item(Items.AMETHYST_CLUSTER) {
        name("<#7fd3ff><bold>Mining Geode")
        lore(
            "",
            "<gray>Used as currency for the event shop",
            "",
            "<#7fd3ff>[Mining Frenzy Currency]"
        )
        shiny()
    }

    @JvmField
    val EFFICIENCY_SIX = item(Items.ENCHANTED_BOOK, 1) {
        name("<aqua>Enchanted Book")

        enchantments(Enchantments.EFFICIENCY to 6)
        shiny()
    }

    @JvmField
    val FORTUNE_THREE = item(Items.ENCHANTED_BOOK, 1) {
        name("<aqua>Enchanted Book")

        enchantments(Enchantments.FORTUNE to 3)
        shiny()
    }
    
    @JvmField
    val ITEM_SHOP = mapOf(
        FIRECRACKERS to 1,
        Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE.defaultInstance to 16,
        Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE.defaultInstance to 16,
        Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE.defaultInstance to 16,
        FORTUNE_THREE to 20,
        DNT_SWIFT_SOAR to 25,
        DNT_ANTIDOTE to 32,
        DNT_OUTREACH to 36,
        EFFICIENCY_SIX to 36,
        Items.NETHERITE_INGOT.defaultInstance to 64,
    )

    fun spawnTrader(location: Location) {
        val trader = WanderingTrader(EntityTypes.WANDERING_TRADER, location.serverLevel)

        trader.yRot = 180.0f
        trader.yHeadRot = 180.0f
        trader.yBodyRot = 180.0f
        trader.setPos(location.toVec3())

        trader.customName = "<aqua><bold>Mining Frenzy Merchant".translated().toNMSComponent()
        trader.isCustomNameVisible = true

        trader.setPersistenceRequired()
        trader.isPermanentlyInvulnerable = true
        trader.isNoAi = true

        val offers = MerchantOffers()

        offers.add(MerchantOffer(shopCost(SHARD_ITEM, 64), GEODE_ITEM.copyWithCount(1), Int.MAX_VALUE, 0, 0f))
        offers.add(MerchantOffer(shopCost(FestivalItems.EW_NATION_CRYPTO_STOCK, 1), GEODE_ITEM.copyWithCount(1), Int.MAX_VALUE, 0, 0f))

        ITEM_SHOP.forEach { (itemStack, price) ->
            offers.add(MerchantOffer(shopCost(GEODE_ITEM ,price), itemStack, Int.MAX_VALUE, 0, 0f))
        }

        trader.offers.clear()
        trader.offers.addAll(offers)

        location.serverLevel.addFreshEntity(trader)
    }

    fun shopCost(item: ItemStack, cost: Int) = ItemCost(
        item.item.builtInRegistryHolder(),
        cost,
        DataComponentExactPredicate.allOf(item.components)
    )
    
}