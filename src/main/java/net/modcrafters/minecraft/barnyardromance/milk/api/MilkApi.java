package net.modcrafters.minecraft.barnyardromance.milk.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.api.SizeClass;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// What other features ask of milk.
public interface MilkApi {
    // Whether the animal's species gives milk (built in: cows, mooshrooms, goats and sheep; a species entry's milk list
    // can add or take away).
    boolean givesMilk(Animal animal);

    // Whether the held item milks this animal: it's on the species' milk list and what it makes exists.
    boolean milksWith(Animal animal, ItemStack held);

    // Whether the species has a milk list and this item, which milks something, isn't on it: the item must do nothing
    // here, not even what it would elsewhere. False for an untracked animal.
    boolean blocksMilkingItem(Animal animal, ItemStack held);

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
