package net.modcrafters.minecraft.barnyardromance.milk.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.SizeClass;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// What other features ask of milk.
public interface MilkApi {
    // Whether the animal's species gives milk (cows, mooshrooms, goats and sheep).
    boolean givesMilk(Animal animal);

    // A tracked female's milk, if she has any. now is the game time, clock the overworld clock time.
    Optional<MilkStatus> status(Animal animal, AnimalTraits traits, long now, long clock);

    // The same for a female whose species gives milk, from her traits alone (the herd book's records).
    Optional<MilkStatus> status(AnimalTraits traits, SizeClass size, long now, long clock);

    // The mother's traits with her milk started by a birth, flowing while her young are babies (until youngUntil).
    AnimalTraits inMilkAfterBirth(AnimalTraits mother, long now, long youngUntil);

    // A superfood that starts milk (Dairy Mash), fed by hand. Null leaves the item to vanilla.
    @Nullable InteractionResult startMilk(Animal animal, Player player, InteractionHand hand, Feed.Eaten eaten);

    // An ordinary food that starts milk: starts a dry, familiar female's milk as she eats it, and says nothing if not.
    void startMilkWithMeal(Animal animal);
}
