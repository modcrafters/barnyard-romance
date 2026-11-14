package net.modcrafters.minecraft.barnyardromance.pregnancy.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.config.AgeSettings;
import net.modcrafters.minecraft.barnyardromance.core.config.Range;
import net.modcrafters.minecraft.barnyardromance.core.text.Text;
import net.modcrafters.minecraft.barnyardromance.food.api.Feed;
import net.modcrafters.minecraft.barnyardromance.food.api.FoodEffects;

import java.util.Optional;

// What other features ask of pregnancy. now is the game time, which pregnancies, love and rest count in.
public interface PregnancyApi {
    // The mother's pregnancy, if she is carrying one.
    Optional<Pregnancy> pregnancy(Animal animal);

    boolean isPregnant(Animal animal);

    // Replaces her pregnancy (the herd stick moves one on or back).
    void setPregnancy(Animal animal, Pregnancy pregnancy);

    // Drops a pregnancy she should never have had: a bird from an older save, from before birds laid eggs.
    void endPregnancy(Animal animal);

    // How far along she is, 0 to 1, with food's gained, lost and boosted ticks counted.
    double progress(Pregnancy pregnancy, long now);

    // Real ticks until she is due.
    long ticksUntilDue(Pregnancy pregnancy, long now);

    // The progress as the percent shown: rounded, and never 100 while time is left.
    int percentShown(double progress);

    // The pregnancy line, from a progress and the ticks until she is due.
    Text progressLine(double progress, long ticksUntilDue);

    // The same line for a pregnancy now, naming the father if asked.
    Text progressLine(Pregnancy pregnancy, long now, boolean withFather);

    // The ticks until she is due as a clock, or "due".
    Text dueClock(long ticksUntilDue);

    // A female's fertility now, 0 to 1: each birth wears her out a little, and she recovers with the clock.
    double fertility(AnimalTraits traits, AgeSettings age, long now);

    // Too worn out by births to conceive.
    boolean isWornOut(AnimalTraits traits, AgeSettings age, long now);

    // The game tick her love mode ends, -1 for none.
    long inLoveUntil(AnimalTraits traits);

    // The game tick her rest after breeding ends, -1 for none.
    long restingUntil(AnimalTraits traits);

    // The game tick she last gave birth, -1 for never.
    long lastBirthTick(AnimalTraits traits);

    // The species she last gave birth to, empty for none.
    String lastBirthSpecies(AnimalTraits traits);

    // A wait picked from the range in ticks, shorter for a heavier mother, as a pregnancy's is (an egg's hatching).
    long pickDuration(Range range, double motherWeight, double roll);

    // A superfood for a pregnant mother (Breeder's Mash, Golden Bran), fed by hand.
    InteractionResult feedMother(Animal animal, Player player, ItemStack stack, Feed.Eaten eaten);

    // What an ordinary food with pregnancy effects does to a pregnant mother; nothing for anyone else.
    void feedPregnancy(Animal animal, FoodEffects effects, double factor);
}
