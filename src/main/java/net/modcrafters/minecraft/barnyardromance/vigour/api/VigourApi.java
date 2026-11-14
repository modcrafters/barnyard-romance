package net.modcrafters.minecraft.barnyardromance.vigour.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;

// What other features ask of a male's Vigour, his mating energy. Days are GameDays; lastAteDay is his last meal day
// (-1 for never), as familiarity keeps it.
public interface VigourApi {
    // Whether he has the Vigour for a mating or eggs that cost this much, and, if he's hungry, the health too.
    boolean canPay(Animal male, double cost, double healthCost);

    // Takes that cost from him; a hungry male pays with health as well.
    void pay(Animal male, double cost, double healthCost);

    // A superfood that gives Vigour back (Stud Mash), fed by hand.
    InteractionResult feedMash(Animal animal, Player player, ItemStack stack, Feed.Eaten eaten);

    // Whether his Vigour has been settled for today.
    boolean settled(AnimalTraits traits, long today);

    // His traits with his Vigour settled up to today; the same traits when that changes nothing.
    AnimalTraits settle(AnimalTraits traits, long lastAteDay, long today);

    // His traits after a meal today; lastAteDayBefore is his last meal day before this one.
    AnimalTraits afterMeal(AnimalTraits traits, long lastAteDayBefore, long today);

    // The last day he rolled for a fight with no female in sight, or -1.
    long lastRivalRoll(AnimalTraits traits);

    // His traits with that roll used up for the day.
    AnimalTraits rolledForRivals(AnimalTraits traits, long day);

    // His Vigour, 0 to 1, settled up to today the way the server would.
    double vigour(AnimalTraits traits, long lastAteDay, long today);

    // Whether he's hungry: fed neither yesterday nor today. A hungry male pays for mating with health.
    boolean hungry(long lastAteDay, long today);
}
