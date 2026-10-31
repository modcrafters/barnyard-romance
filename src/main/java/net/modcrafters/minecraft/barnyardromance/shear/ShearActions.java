package net.modcrafters.minecraft.barnyardromance.shear;

import net.modcrafters.minecraft.barnyardromance.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.familiarity.Refusal;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// Once-a-day shearing for sheep (see ShearRule): shears by hand and from dispensers both ask here.
public final class ShearActions {
    public static final Text ENOUGH = Text.of("message.barnyard_romance.shear.enough");
    public static final Text REFUSED = Text.of("message.barnyard_romance.refused.shear");
    public static final Text REFUSED_TODAY = Text.of("message.barnyard_romance.refused.today");

    private ShearActions() {
    }

    public static boolean shearedToday(Animal animal) {
        return tracked(animal).map(traits -> ShearRule.shearedToday(traits, animal.level().getOverworldClockTime())).orElse(false);
    }

    public static boolean refusedToday(Animal animal) {
        return tracked(animal).map(traits -> traits.refusals().shearRefused(GameDay.of(animal.level().getOverworldClockTime()))).orElse(false);
    }

    // Sheared or refused today: no more shearing until midnight. Rolls nothing.
    public static boolean blocked(Animal animal) {
        return shearedToday(animal) || refusedToday(animal);
    }

    // For dispensers: blocked, or a sheep with wool that rolls a refusal now.
    public static boolean withholds(Animal animal) {
        if (blocked(animal)) {
            return true;
        }
        if (animal instanceof Shearable shearable && shearable.readyForShearing() && rollsRefusal(animal)) {
            refuseShearing(animal, null);
            return true;
        }
        return false;
    }

    // Shears on a sheep already sheared today, or one that won't be sheared: refused, and the player is told why. The
    // hand roll happens here only; NeoForge's shears check blocked afterwards, so one click never rolls twice.
    public static @Nullable InteractionResult refuses(Animal animal, Player player, ItemStack stack) {
        if (!stack.is(Items.SHEARS) || animal.isBaby()) {
            return null;
        }
        if (shearedToday(animal) || refusedToday(animal)) {
            if (!animal.level().isClientSide()) {
                VersionCompat.sendOverlay(player, TextComponents.of(shearedToday(animal) ? ENOUGH : REFUSED_TODAY));
            }
            return InteractionResult.FAIL;
        }
        if (!animal.level().isClientSide() && animal instanceof Shearable shearable && shearable.readyForShearing() && rollsRefusal(animal)) {
            refuseShearing(animal, player);
            return InteractionResult.FAIL;
        }
        return null;
    }

    // A refused shearing keeps the wool and costs no familiarity, but the sheep can't be sheared again until midnight.
    public static void refuseShearing(Animal animal, @Nullable Player player) {
        long today = GameDay.of(animal.level().getOverworldClockTime());
        tracked(animal).ifPresent(traits -> Services.DATA.set(animal, traits.withRefusals(traits.refusals().withShear(today))));
        if (player != null) {
            VersionCompat.sendOverlay(player, TextComponents.of(REFUSED));
        }
        FamiliarityActions.refusalCue(animal);
    }

    private static boolean rollsRefusal(Animal animal) {
        return FamiliarityCare.settle(animal)
                .map(traits -> Refusal.refuses(traits.familiarity(), animal.getRandom().nextDouble(), BarnyardConfig.current().tuning()))
                .orElse(false);
    }

    // Counts the shearing for the once-a-day rule, and takes its familiarity cost.
    public static void recordShearing(Animal animal) {
        tracked(animal).ifPresent(traits -> {
            Services.DATA.set(animal, traits.sheared(animal.level().getOverworldClockTime()));
            FamiliarityCare.spend(animal, BarnyardConfig.current().tuning().familiarityShearingCost());
        });
    }

    private static Optional<AnimalTraits> tracked(Animal animal) {
        if (!(animal instanceof Sheep) || !BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
