package net.modcrafters.minecraft.barnyardromance.familiarity.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.config.HungerSettings;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// What other features ask of familiarity: how familiar an animal is, its meals and hungry days, the days it has yet to
// settle, and its refusals. Days are GameDays and clock is an overworld clock time (see GameDay).
public interface FamiliarityApi {
    // Familiarity, 0 to 1.
    double familiarity(AnimalTraits traits);

    // At or above the familiarity threshold.
    boolean isFamiliar(AnimalTraits traits);

    // Whether this much familiarity reaches the threshold.
    boolean isFamiliarAt(double familiarity);

    double familiarThreshold();

    // The most familiar the animal can become: below 1 for one won over as an adult.
    double wildCap(Animal animal, AnimalTraits traits);

    // The same from its traits alone; pet: a wolf, cat or one of the horse family, which have the higher cap.
    double wildCap(AnimalTraits traits, boolean pet);

    // Tames an animal that this much familiarity tames, if it isn't already.
    void tameIfFamiliar(Animal animal, Player player, double familiarity);

    // A newborn's traits with its starting familiarity, from its nutrition (how well its mother knew her keeper, plus
    // any superfood): born into your care, it counts as farm-raised once it has any.
    AnimalTraits withNewbornFamiliarity(AnimalTraits newborn, double nutrition);

    // The traits with familiarity set as a herd stick sets it: going up obeys the wild-caught cap, as feeding would.
    AnimalTraits withFamiliaritySetTo(Animal animal, AnimalTraits traits, double familiarity);

    // The kept traits, with fresh's familiarity and the hunger and origin that go with it (a herd book that stops
    // updating an animal below the threshold).
    AnimalTraits withFamiliarityFrom(AnimalTraits kept, AnimalTraits fresh);

    // The day of its last meal, -1 for never.
    long lastAteDay(AnimalTraits traits);

    boolean wasHungryYesterday(AnimalTraits traits, long today);

    // The clock time it was last fed by hand, -1 for never.
    long lastHandFed(AnimalTraits traits);

    boolean handFedToday(AnimalTraits traits, long clock);

    // What the herd's upkeep has done to it.
    Upkeep upkeep(AnimalTraits traits);

    // Any food is the day's meal: a normal feed, a hurt animal's heal-only meal, and a superfood.
    void recordMeal(Animal animal);

    // A meal from a feeding block of the kind feeder (a FeederKind id), noted as the block's.
    void recordFeederMeal(Animal animal, long clock, String feeder);

    // Milking and shearing cost a little familiarity.
    void spendFamiliarity(Animal animal, double cost);

    // Settles the days up to yesterday, which may cost familiarity and health, and returns the traits as they are now.
    Optional<AnimalTraits> settle(Animal animal);

    Optional<AnimalTraits> settle(Animal animal, long clock);

    // Settles it, and says whether it still has days before today to settle (while it waits for its herd's block).
    boolean isUnsettled(Animal animal, long clock);

    // The first day not yet settled, -1 for an animal never settled.
    long firstUnsettledDay(AnimalTraits traits);

    // The traits as they would be after today's settle, for an animal far from home that goes on missing meals.
    AnimalTraits afterMissedDays(AnimalTraits traits, long today, boolean nursing);

    // Of missedDays missed, the days the config's catch-up mode counts.
    long daysCounted(long missedDays, HungerSettings.CatchUp mode);

    // The traits with the days before day passed over uncounted, as the catch-up mode passes them over.
    AnimalTraits skippingDaysBefore(AnimalTraits traits, long day);

    // One day of a feeding block's catch-up for an animal settled up to that day, by the same rules as a settle; feeder
    // is the kind of block that fed it that day, if one did. The damage isn't dealt: owed, what the block owes it so
    // far, comes off its health first, and takeToll deals it later.
    UpkeepOutcome settleHerdDay(Animal animal, ServerLevel level, AnimalTraits traits, long day, boolean ate, @Nullable String feeder,
                                float owed);

    // What settled days leave behind: the hunger damage, then the runaway if it's still alive to run.
    void takeToll(Animal animal, ServerLevel level, float damage, boolean escaped, boolean catchingUp);

    // The traits marked in a feeding block's herd or out of it.
    AnimalTraits withInHerd(AnimalTraits traits, boolean inHerd);

    // Too unfamiliar to give milk, wool or stew.
    boolean deniesProducts(Animal animal);

    // Whether an animal this familiar refuses a milking or shearing, for a roll from 0 to 1.
    boolean refuses(AnimalTraits traits, double roll);

    boolean milkRefusedOn(AnimalTraits traits, long day);

    boolean shearRefusedOn(AnimalTraits traits, long day);

    // The traits with a refused milking recorded on day: no more milking until midnight.
    AnimalTraits withMilkRefusedOn(AnimalTraits traits, long day);

    // The traits with a refused shearing recorded on day: no more shearing until midnight.
    AnimalTraits withShearRefusedOn(AnimalTraits traits, long day);

    // The animal makes its noise and looks cross, so the player sees why it refused.
    void showRefusal(Animal animal);
}
