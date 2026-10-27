package net.modcrafters.minecraft.barnyardromance.food;

import net.modcrafters.minecraft.barnyardromance.config.Tuning;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodHealTest {
    @Test
    void explicitHealWins() {
        assertEquals(20.0f, FoodHeal.amount(Optional.of(20.0), OptionalInt.of(3), 1.0, Tuning.DEFAULTS), 1.0e-6f);
    }

    @Test
    void playerFoodHealsHalfItsNutrition() {
        assertEquals(1.5f, FoodHeal.amount(Optional.empty(), OptionalInt.of(3), 1.0, Tuning.DEFAULTS), 1.0e-6f);
    }

    @Test
    void nonFoodHealsTheDefault() {
        assertEquals(1.0f, FoodHeal.amount(Optional.empty(), OptionalInt.empty(), 1.0, Tuning.DEFAULTS), 1.0e-6f);
    }

    @Test
    void factorScalesHeal() {
        assertEquals(0.375f, FoodHeal.amount(Optional.empty(), OptionalInt.of(3), 0.25, Tuning.DEFAULTS), 1.0e-6f);
    }
}
