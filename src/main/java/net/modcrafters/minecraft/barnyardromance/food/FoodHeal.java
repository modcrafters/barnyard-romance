package net.modcrafters.minecraft.barnyardromance.food;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;

import java.util.Optional;
import java.util.OptionalInt;

public final class FoodHeal {
    private FoodHeal() {
    }

    // A food file's heal wins; a player food heals part of its nutrition; anything else heals the default.
    public static float amount(Optional<Double> explicitHeal, OptionalInt nutrition, double factor, Tuning tuning) {
        double base = explicitHeal.orElseGet(() -> nutrition.isPresent()
                ? tuning.foodHealFactor() * nutrition.getAsInt()
                : tuning.foodHealDefault());
        return (float) (Math.max(0.0, base) * Math.max(0.0, factor));
    }
}
