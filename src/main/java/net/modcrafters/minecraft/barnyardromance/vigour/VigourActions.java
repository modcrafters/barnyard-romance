package net.modcrafters.minecraft.barnyardromance.vigour;

import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.core.age.AgeController;
import net.modcrafters.minecraft.barnyardromance.familiarity.api.FamiliarityApi;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

import net.modcrafters.minecraft.barnyardromance.core.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.core.text.TextComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static net.modcrafters.minecraft.barnyardromance.vigour.VigourSlices.VIGOUR;

// A male's Vigour in the world: read it settled, check what he can afford, and take the payment. clock is an
// overworld clock time; the overloads without one use the animal's level.
public final class VigourActions {
    public static final Text NEEDS_MALE = Text.of("message.barnyard_romance.superfood.needs_male");
    // The same words pregnancy has for a mother who can't eat any more.
    public static final Text CANT_EAT_MORE = Text.of("message.barnyard_romance.superfood.cant_eat_more");
    private static final double EPSILON = 1.0e-9;

    private VigourActions() {
    }

    // Stud Mash: a grown, familiar male gets Vigour back. It skips the once-a-day rule like the other superfoods and is
    // no meal: his feeding day, hunger, health and familiarity are left alone.
    public static InteractionResult feedMash(Animal animal, Player player, ItemStack stack, Feed.Eaten eaten) {
        if (animal.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Optional<AnimalTraits> traits = current(animal);
        if (traits.isEmpty() || traits.get().sex() != Sex.MALE || !AgeController.grown(animal)
                || !FeaturesRepository.get(FamiliarityApi.class).isFamiliar(traits.get())) {
            player.sendSystemMessage(TextComponents.of(NEEDS_MALE));
            return InteractionResult.CONSUME;
        }
        if (traits.get().get(VIGOUR).value() >= 1.0 - EPSILON) {
            player.sendSystemMessage(TextComponents.of(CANT_EAT_MORE));
            return InteractionResult.CONSUME;
        }
        if (!player.hasInfiniteMaterials()) {
            stack.consume(1, player);
        }
        Services.DATA.set(animal, traits.get().with(VIGOUR, VigourRules.add(traits.get().get(VIGOUR), eaten.effects().vigour() * eaten.factor())));
        VersionCompat.playEatingSound(animal, stack);
        return VersionCompat.SUCCESS_SERVER;
    }

    public static Optional<AnimalTraits> current(Animal male) {
        return current(male, male.level().getOverworldClockTime());
    }

    public static Optional<AnimalTraits> current(Animal male, long clock) {
        return FeaturesRepository.get(FamiliarityApi.class).settle(male, clock);
    }

    public static boolean hungry(Animal male) {
        return hungry(male, male.level().getOverworldClockTime());
    }

    public static boolean hungry(Animal male, long clock) {
        return current(male, clock).map(traits -> VigourRules.hungry(FeaturesRepository.get(FamiliarityApi.class).lastAteDay(traits), GameDay.of(clock))).orElse(false);
    }

    public static boolean canPay(Animal male, double cost, double healthCost) {
        return canPay(male, cost, healthCost, male.level().getOverworldClockTime());
    }

    public static boolean canPay(Animal male, double cost, double healthCost, long clock) {
        Optional<AnimalTraits> traits = current(male, clock);
        // Until his days are settled (one in a feeding block's herd may wait a few ticks for it), neither his Vigour
        // nor whether he's hungry is known, so he waits too.
        FamiliarityApi familiarityApi = FeaturesRepository.get(FamiliarityApi.class);
        if (traits.isEmpty() || familiarityApi.firstUnsettledDay(traits.get()) < GameDay.of(clock)) {
            return false;
        }
        boolean hungry = VigourRules.hungry(familiarityApi.lastAteDay(traits.get()), GameDay.of(clock));
        return VigourRules.canPay(traits.get().get(VIGOUR).value(), cost, hungry, male.getHealth(), healthCost);
    }

    public static void pay(Animal male, double cost, double healthCost) {
        pay(male, cost, healthCost, male.level().getOverworldClockTime());
    }

    // A hungry male pays with health as well; it's set directly, so nothing hurts him and nothing panics. canPay keeps
    // him from mating if it would leave him under 1; the floor here only covers a male hurt since he was checked.
    public static void pay(Animal male, double cost, double healthCost, long clock) {
        current(male, clock).ifPresent(traits -> {
            Services.DATA.set(male, traits.with(VIGOUR, VigourRules.pay(traits.get(VIGOUR), cost)));
            if (VigourRules.hungry(FeaturesRepository.get(FamiliarityApi.class).lastAteDay(traits), GameDay.of(clock)) && healthCost > 0.0) {
                male.setHealth(Math.max(1.0F, male.getHealth() - (float) healthCost));
            }
        });
    }
}
