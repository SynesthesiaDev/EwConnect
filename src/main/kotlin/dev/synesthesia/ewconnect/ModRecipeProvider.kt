package dev.synesthesia.ewconnect

import dev.synesthesia.ewconnect.items.RecoveryEcho
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancements.Advancement
import net.minecraft.core.HolderLookup
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe
import java.util.concurrent.CompletableFuture

class ModRecipeProvider(
    packOutput: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>
) : FabricRecipeProvider(packOutput, registriesFuture) {

    override fun createRecipeProvider(
        registries: HolderLookup.Provider,
        recipes: BootstrapContext<Recipe<*>>,
        advancements: BootstrapContext<Advancement>
    ): RecipeProvider {
        return object : RecipeProvider(recipes, advancements) {
            override fun buildRecipes() {
                val item = RecoveryEcho().itemStackTemplate

                shapeless(RecipeCategory.MISC, item)
                    .requires(Items.SOUL_SOIL, 4)
                    .requires(Items.GHAST_TEAR, 4)
                    .requires(Items.END_CRYSTAL)
                    .unlockedBy(getHasName(Items.RECOVERY_COMPASS), has(Items.RECOVERY_COMPASS))
                    .save(output)
            }
        }
    }

    override fun getName(): String = "ModRecipeProvider"
}