package net.modcrafters.minecraft.barnyardromance.shear;

import net.modcrafters.minecraft.barnyardromance.compat.VersionCompat;
import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalSex;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.text.Text;
import net.modcrafters.minecraft.barnyardromance.text.TextComponents;
import net.minecraft.world.InteractionResult;
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

    private ShearActions() {
    }

    public static boolean shearedToday(Animal animal) {
        return tracked(animal).map(traits -> ShearRule.shearedToday(traits, animal.level().getOverworldClockTime())).orElse(false);
    }

    // Shears on a sheep already sheared today: refused, and the player is told why.
    public static @Nullable InteractionResult refuses(Animal animal, Player player, ItemStack stack) {
        if (!stack.is(Items.SHEARS) || animal.isBaby() || !shearedToday(animal)) {
            return null;
        }
        if (!animal.level().isClientSide()) {
            VersionCompat.sendOverlay(player, TextComponents.of(ENOUGH));
        }
        return InteractionResult.FAIL;
    }

    public static void recordShearing(Animal animal) {
        tracked(animal).ifPresent(traits -> Services.DATA.set(animal, traits.sheared(animal.level().getOverworldClockTime())));
    }

    private static Optional<AnimalTraits> tracked(Animal animal) {
        if (!(animal instanceof Sheep) || !BarnyardConfig.current().speciesEnabled(AnimalSex.speciesId(animal))) {
            return Optional.empty();
        }
        return Services.DATA.get(animal);
    }
}
