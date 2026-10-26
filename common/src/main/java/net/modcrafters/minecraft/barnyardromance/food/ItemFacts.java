package net.modcrafters.minecraft.barnyardromance.food;

import java.util.OptionalInt;

// What the rules need to know about the held item, so they stay free of Minecraft classes.
public interface ItemFacts {
    boolean matches(ItemMatch match);

    // The species' own vanilla food (Animal#isFood).
    boolean vanillaFood();

    // Hunger points when a player could eat it.
    OptionalInt nutrition();

    default boolean matchesAny(Iterable<ItemMatch> matches) {
        for (ItemMatch match : matches) {
            if (matches(match)) {
                return true;
            }
        }
        return false;
    }
}
