package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.herd.DeathCause;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.herd.HerdRecord;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.rivalry.Courtship;
import net.modcrafters.minecraft.barnyardromance.rivalry.Fights;
import net.modcrafters.minecraft.barnyardromance.rivalry.RivalRolls;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.eggs.EggSchedule;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Lineage;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.CourtshipGameTests.assertFather;
import static net.modcrafters.minecraft.barnyardromance.gametest.CourtshipGameTests.fed;

// In-game checks for males fighting: over a female in love, to the death, and what stops a fight. Most start the
// fight by hand, since whether rivals fight over her is a roll; those that need the roll call Courtship.court with
// one. Paths can't be found on the tick animals appear, so courting waits a few ticks.
public final class FightGameTests {
    // Paths can't be found on the tick animals appear, nor while a new chicken is still fluttering down.
    private static final int SETTLE_TICKS = 40;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("two_bulls_fight_over_a_cow_until_one_dies", 1200, FightGameTests::twoBullsFightOverACowUntilOneDies),
            new BreedingGameTests.Case("a_third_bull_mates_while_two_fight", 400, FightGameTests::aThirdBullMatesWhileTwoFight),
            new BreedingGameTests.Case("four_bulls_fight_in_two_pairs", 100, FightGameTests::fourBullsFightInTwoPairs),
            new BreedingGameTests.Case("the_bigger_bull_wins", 1200, FightGameTests::theBiggerBullWins),
            new BreedingGameTests.Case("food_in_hand_stops_a_fight_between_familiar_bulls", 100, FightGameTests::foodInHandStopsAFightBetweenFamiliarBulls),
            new BreedingGameTests.Case("food_already_in_hand_does_not_stop_a_fight", 200, FightGameTests::foodAlreadyInHandDoesNotStopAFight),
            new BreedingGameTests.Case("food_does_not_stop_a_wild_fight", 1200, FightGameTests::foodDoesNotStopAWildFight),
            new BreedingGameTests.Case("goats_fight_to_the_end", 1200, FightGameTests::goatsFightToTheEnd),
            new BreedingGameTests.Case("a_fight_ends_when_a_rival_is_removed", 200, FightGameTests::aFightEndsWhenARivalIsRemoved),
            new BreedingGameTests.Case("bystanders_are_not_hurt", 1200, FightGameTests::bystandersAreNotHurt),
            new BreedingGameTests.Case("pets_never_fight", 100, FightGameTests::petsNeverFight),
            new BreedingGameTests.Case("a_rival_death_is_noted_in_the_herd_book", 1200, FightGameTests::aRivalDeathIsNotedInTheHerdBook),
            new BreedingGameTests.Case("roosters_fight_without_a_hen", 1200, FightGameTests::roostersFightWithoutAHen),
            new BreedingGameTests.Case("familiar_bulls_never_fight_at_random", 100, FightGameTests::familiarBullsNeverFightAtRandom),
            new BreedingGameTests.Case("a_wild_and_a_familiar_bull_never_fight_at_random", 100, FightGameTests::aWildAndAFamiliarBullNeverFightAtRandom),
            new BreedingGameTests.Case("two_wild_bulls_fight_at_random", 100, FightGameTests::twoWildBullsFightAtRandom),
            new BreedingGameTests.Case("a_male_rolls_once_a_day", 100, FightGameTests::aMaleRollsOnceADay)
    );

    private FightGameTests() {
    }

    public static void twoBullsFightOverACowUntilOneDies(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Animal first = bull(helper, 5, 4, 1.0);
        Animal second = bull(helper, 5, 6, 1.0);
        courtWithAFight(helper, cow, first, second);
        helper.runAfterDelay(700, () -> helper.assertTrue(cow.isInLove() || Services.DATA.get(cow, PregnancyFeature.PREGNANCY).isPresent(),
                "the cow's love ran out while the bulls fought"));

        helper.succeedWhen(() -> {
            helper.assertTrue(first.isAlive() != second.isAlive(), "the bulls are " + first.isAlive() + " and " + second.isAlive());
            assertFather(helper, cow, first.isAlive() ? first : second);
        });
    }

    // The bulls wander while they settle, so which two are closest is read off where they stand when she's courted, not
    // where they were put.
    public static void aThirdBullMatesWhileTwoFight(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        List<Animal> closestFirst = new ArrayList<>(List.of(bull(helper, 4, 5, 1.0), bull(helper, 5, 5, 1.0), bull(helper, 7, 7, 1.0)));
        cow.setInLoveTime(600);

        helper.runAfterDelay(SETTLE_TICKS, () -> {
            closestFirst.sort(Comparator.comparingDouble(bull -> bull.distanceToSqr(cow)));
            Courtship.court(cow, 0.0);
            helper.assertTrue(Fights.fighting(closestFirst.get(0)) && Fights.fighting(closestFirst.get(1)) && !Fights.fighting(closestFirst.get(2)),
                    "the two closest bulls didn't start fighting, or the third did: " + distances(closestFirst, cow));
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(Fights.fighting(closestFirst.get(0)) || !closestFirst.get(0).isAlive() || !closestFirst.get(1).isAlive(),
                    "the two closer bulls aren't fighting");
            assertFather(helper, cow, closestFirst.get(2));
        });
    }

    private static String distances(List<Animal> bulls, Animal cow) {
        StringBuilder text = new StringBuilder();
        for (Animal bull : bulls) {
            text.append(String.format(Locale.ROOT, "%.1f ", bull.distanceTo(cow)));
        }
        return text.toString().trim() + " blocks off";
    }

    // Walked closest first, two at a time: both pairs fight, and nobody is left over to mate with her yet.
    public static void fourBullsFightInTwoPairs(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        List<Animal> bulls = List.of(bull(helper, 4, 4, 1.0), bull(helper, 5, 4, 1.0), bull(helper, 6, 6, 1.0), bull(helper, 7, 7, 1.0));
        cow.setInLoveTime(600);

        helper.runAfterDelay(SETTLE_TICKS, () -> {
            Courtship.court(cow, 0.0);
            for (Animal bull : bulls) {
                helper.assertTrue(Fights.fighting(bull), "a bull isn't fighting");
                helper.assertTrue(!Courtship.chosen(cow, bull), "a bull was chosen while all four fight");
            }
            helper.succeed();
        });
    }

    public static void theBiggerBullWins(GameTestHelper helper) {
        Animal big = bull(helper, 3, 4, 1.3);
        Animal small = bull(helper, 5, 4, 0.7);
        Fights.start(big, small, null);

        helper.succeedWhen(() -> helper.assertTrue(big.isAlive() && !small.isAlive(), "big alive " + big.isAlive() + ", small alive " + small.isAlive()));
    }

    public static void foodInHandStopsAFightBetweenFamiliarBulls(GameTestHelper helper) {
        Animal first = bull(helper, 3, 4, 1.0);
        Animal second = bull(helper, 5, 4, 1.0);
        Fights.start(first, second, null);
        playerWith(helper, Items.WHEAT);

        helper.succeedWhen(() -> {
            helper.assertTrue(!Fights.fighting(first) && !Fights.fighting(second), "the bulls are still fighting");
            helper.assertTrue(first.isAlive() && second.isAlive(), "a bull died");
            helper.assertTrue(Fights.inTruce(first, second, helper.getLevel().getGameTime()), "the bulls have no truce");
        });
    }

    // You've just fed her, so you're standing by with wheat in hand: it only stops them once you put it away and take it
    // out again, or walk off and come back.
    public static void foodAlreadyInHandDoesNotStopAFight(GameTestHelper helper) {
        Animal first = bull(helper, 3, 4, 1.0);
        Animal second = bull(helper, 5, 4, 1.0);
        Player player = playerWith(helper, Items.WHEAT);
        Fights.start(first, second, null);
        boolean[] takenOutAgain = {false};

        helper.runAfterDelay(60, () -> {
            helper.assertTrue(Fights.fighting(first) && Fights.fighting(second), "food already in hand stopped the fight");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.runAfterDelay(20, () -> {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
                takenOutAgain[0] = true;
            });
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(takenOutAgain[0], "the wheat hasn't been taken out again yet");
            helper.assertTrue(!Fights.fighting(first) && !Fights.fighting(second), "taking the wheat out again didn't stop them");
            helper.assertTrue(first.isAlive() && second.isAlive(), "a bull died");
        });
    }

    public static void foodDoesNotStopAWildFight(GameTestHelper helper) {
        Animal first = bull(helper, 3, 4, 1.0);
        Animal wild = bull(helper, 5, 4, 1.0);
        Services.DATA.set(wild, traits(helper, wild).fed(0.1, -1L));
        Fights.start(first, wild, null);
        playerWith(helper, Items.WHEAT);

        helper.succeedWhen(() -> helper.assertTrue(first.isAlive() != wild.isAlive(), "both bulls are " + first.isAlive()));
    }

    // A goat's brain panics at any hurt; a fighting goat must stay and fight.
    public static void goatsFightToTheEnd(GameTestHelper helper) {
        Animal first = male(helper, EntityTypes.GOAT, 3, 4, 1.0);
        Animal second = male(helper, EntityTypes.GOAT, 5, 4, 1.0);
        Vec3 start = helper.absoluteVec(new Vec3(4.0, 1.0, 4.0));
        Fights.start(first, second, null);

        helper.succeedWhen(() -> {
            helper.assertTrue(first.isAlive() != second.isAlive(), "both goats are " + first.isAlive());
            Animal survivor = first.isAlive() ? first : second;
            helper.assertTrue(survivor.position().distanceTo(start) < 4.0, "the surviving goat ran " + survivor.position().distanceTo(start) + " blocks off");
        });
    }

    public static void aFightEndsWhenARivalIsRemoved(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Animal first = bull(helper, 5, 4, 1.0);
        Animal second = bull(helper, 5, 6, 1.0);
        courtWithAFight(helper, cow, first, second);
        helper.runAfterDelay(SETTLE_TICKS + 20, second::discard);

        helper.runAfterDelay(SETTLE_TICKS + 60, () -> {
            helper.assertTrue(!Fights.fighting(first), "the bull still fights a rival who is gone");
            int love = cow.getInLoveTime();
            helper.runAfterDelay(5, () -> {
                helper.assertTrue(cow.getInLoveTime() < love || Services.DATA.get(cow, PregnancyFeature.PREGNANCY).isPresent(), "the cow's love is still held");
                helper.succeed();
            });
        });
    }

    public static void bystandersAreNotHurt(GameTestHelper helper) {
        Animal first = bull(helper, 3, 4, 1.0);
        Animal second = bull(helper, 5, 4, 1.0);
        Animal sheep = female(helper, EntityTypes.SHEEP);
        Fights.start(first, second, null);

        helper.succeedWhen(() -> {
            helper.assertTrue(first.isAlive() != second.isAlive(), "the fight isn't over");
            helper.assertTrue(sheep.getHealth() == sheep.getMaxHealth(), "the sheep was hurt");
        });
    }

    public static void petsNeverFight(GameTestHelper helper) {
        Player owner = MockPlayers.inLevel(helper);
        owner.setPos(helper.absoluteVec(new Vec3(4.5, 1.0, 6.5)));
        Animal female = tame(female(helper, EntityTypes.WOLF), owner);
        Animal near = tame(male(helper, EntityTypes.WOLF, 5, 4, 1.0), owner);
        Animal far = tame(male(helper, EntityTypes.WOLF, 6, 6, 1.0), owner);
        female.setInLoveTime(600);

        helper.runAfterDelay(SETTLE_TICKS, () -> {
            Animal closer = near.distanceToSqr(female) <= far.distanceToSqr(female) ? near : far;
            Courtship.court(female, 0.0);
            helper.assertTrue(!Fights.fighting(near) && !Fights.fighting(far), "wolves fought over a female");
            helper.assertTrue(Courtship.chosen(female, closer), "the closer wolf wasn't chosen");
            helper.succeed();
        });
    }

    public static void aRivalDeathIsNotedInTheHerdBook(GameTestHelper helper) {
        Animal big = bull(helper, 3, 4, 1.3);
        Animal small = bull(helper, 5, 4, 0.7);
        // The book keeps a dead animal only while a living member descends from it.
        ItemStack book = new ItemStack(BarnyardItems.HERD_BOOK);
        Animal calf = GameTestCompat.spawn(helper, EntityTypes.COW, 2, 1, 2);
        calf.setBaby(true);
        Services.DATA.set(calf, new AnimalTraits(Sex.FEMALE, 1.0, 1.0, -1L).withLineage(new Lineage(null, small.getUUID())));
        HerdBooks.register(book, small);
        HerdBooks.register(book, calf);
        Fights.start(big, small, null);

        helper.succeedWhen(() -> {
            HerdRecord record = HerdBookData.get(helper.getLevel().getServer()).record(small.getUUID());
            helper.assertTrue(record != null && !record.alive(), "the small bull's record is " + record);
            helper.assertTrue(record.cause() == DeathCause.RIVAL, "the small bull died of " + record.cause());
        });
    }

    // Unfed and wild, with no hen in sight: roosters fight anyway.
    public static void roostersFightWithoutAHen(GameTestHelper helper) {
        Animal first = male(helper, EntityTypes.CHICKEN, 3, 4, 1.0);
        Animal second = male(helper, EntityTypes.CHICKEN, 5, 4, 1.0);
        Services.DATA.set(first, traits(helper, first).fed(0.0, -1L));
        Services.DATA.set(second, traits(helper, second).fed(0.0, -1L));
        rollAfterSettling(helper, first, second, 0.0, true);

        helper.succeedWhen(() -> helper.assertTrue(first.isAlive() != second.isAlive(), "both roosters are " + first.isAlive()
                + ": fighting " + Fights.fighting(first) + "/" + Fights.fighting(second) + " dist=" + first.distanceTo(second)
                + " health=" + first.getHealth() + "/" + second.getHealth() + " ground=" + first.onGround() + "/" + second.onGround()
                + " pos=" + helper.relativePos(first.blockPosition()) + "/" + helper.relativePos(second.blockPosition())));
    }

    public static void familiarBullsNeverFightAtRandom(GameTestHelper helper) {
        Animal first = bull(helper, 3, 4, 1.0);
        Animal second = bull(helper, 5, 4, 1.0);
        rollAfterSettling(helper, first, second, 0.0, false);
    }

    public static void aWildAndAFamiliarBullNeverFightAtRandom(GameTestHelper helper) {
        Animal first = bull(helper, 3, 4, 1.0);
        Animal wild = bull(helper, 5, 4, 1.0);
        Services.DATA.set(wild, traits(helper, wild).fed(0.1, -1L));
        rollAfterSettling(helper, first, wild, 0.0, false);
    }

    public static void twoWildBullsFightAtRandom(GameTestHelper helper) {
        Animal first = bull(helper, 3, 4, 1.0);
        Animal second = bull(helper, 5, 4, 1.0);
        Services.DATA.set(first, traits(helper, first).fed(0.1, -1L));
        Services.DATA.set(second, traits(helper, second).fed(0.1, -1L));
        rollAfterSettling(helper, first, second, 0.0, true);

        helper.succeedWhen(() -> helper.assertTrue(Fights.fighting(first) || !first.isAlive() || !second.isAlive(), "the wild bulls never fought"));
    }

    // A roll that comes up "no fight" still uses the day: the male isn't due again until tomorrow.
    // Familiar bulls never fight at random, so no roll of their own can get in the way.
    public static void aMaleRollsOnceADay(GameTestHelper helper) {
        Animal first = bull(helper, 3, 4, 1.0);
        Animal second = bull(helper, 5, 4, 1.0);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            Animal roller = first.getUUID().compareTo(second.getUUID()) < 0 ? first : second;
            long clock = helper.getLevel().getOverworldClockTime();
            RivalRolls.roll(roller, helper.getLevel(), 1.0);
            helper.assertTrue(!Fights.fighting(roller), "a roll of 1.0 started a fight");
            helper.assertTrue(traits(helper, roller).vigour().rollDay() == GameDay.of(clock), "the roll wasn't recorded for today");
            helper.assertTrue(!RivalRolls.due(roller, clock + 23_000L - EggSchedule.sinceMidnight(clock)), "the bull is due again the same day");
            helper.succeed();
        });
    }

    // The male with the smaller UUID rolls for the pair, so a pair rolls once. The roll is passed in so the outcome is
    // certain; a pair that may fight does at 0.0.
    private static void rollAfterSettling(GameTestHelper helper, Animal first, Animal second, double roll, boolean fight) {
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            Animal roller = first.getUUID().compareTo(second.getUUID()) < 0 ? first : second;
            RivalRolls.roll(roller, helper.getLevel(), roll);
            helper.assertTrue(Fights.fighting(first) == fight && Fights.fighting(second) == fight,
                    (fight ? "the males didn't start fighting" : "the males started fighting"));
            helper.assertTrue(traits(helper, roller).vigour().rollDay() == GameDay.of(helper.getLevel().getOverworldClockTime()),
                    "the roll wasn't recorded for today");
            if (!fight) {
                helper.succeed();
            }
        });
    }

    // Courts once the animals have settled, with a roll that always means a fight, and checks the fight began.
    private static void courtWithAFight(GameTestHelper helper, Animal cow, Animal first, Animal second) {
        cow.setInLoveTime(600);
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            Courtship.court(cow, 0.0);
            helper.assertTrue(Fights.fighting(first) && Fights.fighting(second), "the two closest bulls didn't start fighting");
        });
    }

    private static Animal bull(GameTestHelper helper, int x, int z, double scale) {
        return male(helper, EntityTypes.COW, x, z, scale);
    }

    private static Animal male(GameTestHelper helper, EntityType<? extends Animal> type, int x, int z, double scale) {
        Animal animal = GameTestCompat.spawn(helper, type, x, 1, z);
        Services.DATA.set(animal, new AnimalTraits(Sex.MALE, scale, 1.0, -1L));
        return fed(animal);
    }

    // Standing in the pen with food in hand, so the bulls can see it.
    private static Player playerWith(GameTestHelper helper, net.minecraft.world.item.Item item) {
        Player player = MockPlayers.inLevel(helper);
        player.setPos(helper.absoluteVec(new Vec3(4.5, 1.0, 6.5)));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        return player;
    }

    private static Animal tame(Animal wolf, Player owner) {
        TamableAnimal pet = (TamableAnimal) wolf;
        pet.tame(owner);
        pet.setOrderedToSit(false);
        pet.setInSittingPose(false);
        return wolf;
    }
}
