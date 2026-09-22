package dev.synesthesia.ewconnect.event.bonfire

import dev.synesthesia.ewconnect.extensions.toNMSComponent
import dev.synesthesia.ewconnect.extensions.translated
import dev.synesthesia.ewconnect.utils.DungeonsAndTaverns
import dev.synesthesia.ewconnect.utils.Location
import dev.synesthesia.ewconnect.utils.item
import net.minecraft.advancements.predicates.EnchantmentPredicate
import net.minecraft.core.component.predicates.EnchantmentsPredicate
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.trading.MerchantOffer
import net.minecraft.world.item.trading.MerchantOffers

object FestivalItems {

    @JvmField
    val FIRECRACKERS = item(Items.SNOWBALL, 4) {
        name("<red><bold>Festival Firecrackers")
        lore(
            "",
            "<white>Right-click <gray>to spawn powerful pyrotechnics!",
            "<gray>Only slightly dangerous!",
            "",
            "<yellow>[Bonfire Festival 2026 Reward]",
            ""
        )
        shiny()
    }

    @JvmField
    val EW_NATION_CRYPTO_STOCK = item(Items.PAPER) {
        name("<aqua>EW NATION CRYPTO STOCK!!")
        lore(
            "",
            "<gray>BUY NOW, PRICE WILL GO UP!!",
            "<gray>SELL LATER, PROFIT!!!!",
            "<gray>NOT A SCAM!!!!!",
            "",
            "<yellow>[Bonfire Festival 2026 Reward]",
        )
    }

    @JvmField
    val DECORATIVE_HEAD_VOUCHER = item(Items.PAPER, 1) {
        name("<aqua><bold>Custom Decorative Head Voucher")
        lore(
            "",
            "<gray>Contact Maya to get it!",
            "",
            "<yellow>[Bonfire Festival 2026 Reward]",
        )
        shiny()
    }

    @JvmField
    val MAYAS_BUILD_FAVOUR = item(Items.PAPER, 1) {
        name("<light_purple><bold>Maya's building favour")
        lore(
            "",
            "<gray>Maya will build something (reasonable)",
            "<gray>for you! Contact Maya to get it!",
            "",
            "<yellow>[Bonfire Festival 2026 Reward]",
        )
        shiny()
    }

    @JvmField
    val MAYAS_ARMOR_STAND_FAVOUR = item(Items.PAPER, 1) {
        name("<green><bold>Maya's armor stand favour")
        lore(
            "",
            "<gray>Maya will make a cool custom armor stand",
            "<gray>for you! Contact Maya to get it!",
            "",
            "<yellow>[Bonfire Festival 2026 Reward]",
        )
        shiny()
    }

    @JvmField
    val BURGER = item(Items.BREAD, 1) {
        name("<white>borger..")
        lore(
            "",
            "<gray>zara will kill me",
            "<gray>-maya",
            "",
            "<yellow>[Bonfire Festival 2026 Reward]",
        )
        shiny()
    }

    @JvmField
    val DNT_OUTREACH = item(Items.ENCHANTED_BOOK, 1) {
        name("<aqua>Enchanted Book")
        lore(
            "",
            "<gray>Increases block interaction range per level,",
            "<gray>allowing you to place or break blocks from farther.",
            ""
        )
        
        enchantments(DungeonsAndTaverns.OUTREACH to 1)
        
        shiny()
    }


    @JvmField
    val DNT_ANTIDOTE = item(Items.ENCHANTED_BOOK, 1) {
        name("<aqua>Enchanted Book")
        lore(
            "",
            "<gray>Negates all damage from Poison and Wither",
            "<gray>effects and deals it to the chestplate.",
            "",
            "<dark_gray>(Incompatible with Protection)",
        )

        enchantments(DungeonsAndTaverns.ANTIDOTE to 1)
        shiny()
    }

    val DNT_SWIFT_SOAR = item(Items.ENCHANTED_BOOK, 1) {
        name("<aqua>Enchanted Book")
        lore(
            "",
            "<gray>Boosts the Happy Ghast's flying speed",
            "<gray>while the rider is considered sprinting",
            "",
        )

        enchantments(DungeonsAndTaverns.SWIFT_SOAR to 1)
        shiny()
    }
    
    @JvmField
    val ITEM_SHOP = mapOf(
        EW_NATION_CRYPTO_STOCK to 1,
        BURGER to 1,
        FIRECRACKERS to 1,
        DECORATIVE_HEAD_VOUCHER to 3,

        Items.MUSIC_DISC_LAVA_CHICKEN.defaultInstance to 4,
        Items.MUSIC_DISC_CREATOR_MUSIC_BOX.defaultInstance to 4,
        Items.MUSIC_DISC_BOUNCE.defaultInstance to 4,

        Items.SULFUR_CUBE_BUCKET.defaultInstance to 5,

        MAYAS_ARMOR_STAND_FAVOUR to 5,
        MAYAS_BUILD_FAVOUR to 6,

        DNT_ANTIDOTE to 7,
        DNT_SWIFT_SOAR to 7,
        DNT_OUTREACH to 9
    )

    fun spawnTrader(location: Location) {
        val trader = WanderingTrader(EntityTypes.WANDERING_TRADER, location.serverLevel)

        trader.yRot = 180.0f
        trader.yHeadRot = 180.0f
        trader.yBodyRot = 180.0f
        trader.setPos(location.toVec3())
        
        trader.customName = "<light_purple><bold>Festival Merchant".translated().toNMSComponent()
        trader.isCustomNameVisible = true

        trader.setPersistenceRequired()
        trader.isPermanentlyInvulnerable = true
        trader.isNoAi = true

        val offers = MerchantOffers()
        ITEM_SHOP.forEach { (itemStack, price) ->
            offers.add(MerchantOffer(BonfireFestivalEvent.festivalTicketCost(price), itemStack, Int.MAX_VALUE, 0, 0f))
        }

        trader.offers.clear()
        trader.offers.addAll(offers)
        
        location.serverLevel.addFreshEntity(trader)
    }
}