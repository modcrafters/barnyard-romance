package net.modcrafters.minecraft.barnyardromance.gametest.legacy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

// 1.20.2's RecipeHolder, which the shared game tests import in its place on 1.20.1 (see stonecutter.gradle.kts): a
// recipe and its id. A 1.20.1 recipe carries its own id; GameTestCompat.craftingRecipe puts the two in one of these.
public record RecipeHolder<T extends Recipe<?>>(ResourceLocation id, T value) {
}
