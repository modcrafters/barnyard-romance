package net.modcrafters.minecraft.barnyardromance.vigour;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

// A male's Vigour in the world: read it settled, check what he can afford, and take the payment. clock is an
// overworld clock time; the overloads without one use the animal's level.
public final class VigourActions {
    private VigourActions() {
    }

    public static Optional<AnimalTraits> current(Animal male) {
        return current(male, male.level().getOverworldClockTime());
    }

    public static Optional<AnimalTraits> current(Animal male, long clock) {
        return FamiliarityCare.settle(male, clock);
    }

    public static boolean hungry(Animal male) {
        return hungry(male, male.level().getOverworldClockTime());
    }

    public static boolean hungry(Animal male, long clock) {
        return current(male, clock).map(traits -> VigourRules.hungry(traits.hunger().lastAteDay(), GameDay.of(clock))).orElse(false);
    }

    public static boolean canPay(Animal male, double cost, double healthCost) {
        return canPay(male, cost, healthCost, male.level().getOverworldClockTime());
    }

    public static boolean canPay(Animal male, double cost, double healthCost, long clock) {
        Optional<AnimalTraits> traits = current(male, clock);
        if (traits.isEmpty()) {
            return false;
        }
        boolean hungry = VigourRules.hungry(traits.get().hunger().lastAteDay(), GameDay.of(clock));
        return VigourRules.canPay(traits.get().vigour().value(), cost, hungry, male.getHealth(), healthCost);
    }

    public static void pay(Animal male, double cost, double healthCost) {
        pay(male, cost, healthCost, male.level().getOverworldClockTime());
    }

    // A hungry male pays with health as well; it's set directly, so nothing hurts him and nothing panics.
    public static void pay(Animal male, double cost, double healthCost, long clock) {
        current(male, clock).ifPresent(traits -> {
            Services.DATA.set(male, traits.withVigour(VigourRules.pay(traits.vigour(), cost)));
            if (VigourRules.hungry(traits.hunger().lastAteDay(), GameDay.of(clock)) && healthCost > 0.0) {
                male.setHealth(Math.max(1.0F, male.getHealth() - (float) healthCost));
            }
        });
    }
}
