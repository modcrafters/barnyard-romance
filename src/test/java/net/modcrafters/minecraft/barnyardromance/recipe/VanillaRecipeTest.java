package net.modcrafters.minecraft.barnyardromance.recipe;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class VanillaRecipeTest {
    // Only one mod's copy of a vanilla recipe survives, and which one depends on load order.
    // Naturalist replaces cake too, so the mod adds its own recipe beside vanilla's.
    @Test
    void doesNotReplaceVanillaCake() {
        assertNull(getClass().getClassLoader().getResource("data/minecraft/recipe/cake.json"));
    }
}
