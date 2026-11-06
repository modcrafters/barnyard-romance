package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.herd.DeathCause;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBookData;
import net.modcrafters.minecraft.barnyardromance.herd.HerdBooks;
import net.modcrafters.minecraft.barnyardromance.herd.HerdRecord;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.rivalry.Courtship;
import net.modcrafters.minecraft.barnyardromance.rivalry.Fights;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Lineage;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
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

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.CourtshipGameTests.assertFather;
import static net.modcrafters.minecraft.barnyardromance.gametest.CourtshipGameTests.fed;

// In-game checks for males fighting: over a female in love, to the death, and what stops a fight. Most start the
// fight by hand, since whether rivals fight over her is a roll; those that need the roll call Courtship.court with
// one. Paths can't be found on the tick animals appear, so courting waits a few ticks.
public final class FightGameTests {
    private static final int SETTLE_TICKS = 10;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("two_bulls_fight_over_a_cow_until_one_dies", 1200, FightGameTests::twoBullsFightOverACowUntilOneDies),
            new BreedingGameTests.Case("a_third_bull_mates_while_two_fight", 400, FightGameTests::aThirdBullMatesWhileTwoFight),
            new BreedingGameTests.Case("the_bigger_bull_wins", 1200, FightGameTests::theBiggerBullWins),
            new BreedingGameTests.Case("food_in_hand_stops_a_fight_between_familiar_bulls", 100, FightGameTests::foodInHandStopsAFightBetweenFamiliarBulls),
            new BreedingGameTests.Case("food_does_not_stop_a_wild_fight", 1200, FightGameTests::foodDoesNotStopAWildFight),
            new BreedingGameTests.Case("goats_fight_to_the_end", 1200, FightGameTests::goatsFightToTheEnd),
            new BreedingGameTests.Case("a_fight_ends_when_a_rival_is_removed", 100, FightGameTests::aFightEndsWhenARivalIsRemoved),
            new BreedingGameTests.Case("bystanders_are_not_hurt", 1200, FightGameTests::bystandersAreNotHurt),
            new BreedingGameTests.Case("pets_never_fight", 100, FightGameTests::petsNeverFight),
            new BreedingGameTests.Case("a_rival_death_is_noted_in_the_herd_book", 1200, FightGameTests::aRivalDeathIsNotedInTheHerdBook)
    );

    private FightGameTests() {
    }

    public static void twoBullsFightOverACowUntilOneDies(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Animal first = bull(helper, 5, 4, 1.0);
        Animal second = bull(helper, 5, 6, 1.0);
        courtWithAFight(helper, cow, first, second);
        helper.runAfterDelay(700, () -> helper.assertTrue(cow.isInLove() || Services.DATA.getPregnancy(cow).isPresent(),
                "the cow's love ran out while the bulls fought"));

        helper.succeedWhen(() -> {
            helper.assertTrue(first.isAlive() != second.isAlive(), "the bulls are " + first.isAlive() + " and " + second.isAlive());
            assertFather(helper, cow, first.isAlive() ? first : second);
        });
    }

    public static void aThirdBullMatesWhileTwoFight(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        Animal first = bull(helper, 4, 5, 1.0);
        Animal second = bull(helper, 5, 5, 1.0);
        Animal third = bull(helper, 7, 7, 1.0);
        courtWithAFight(helper, cow, first, second);

        helper.succeedWhen(() -> {
            assertFather(helper, cow, third);
            helper.assertTrue(Fights.fighting(first) || !first.isAlive() || !second.isAlive(), "the two closer bulls aren't fighting");
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
                helper.assertTrue(cow.getInLoveTime() < love || Services.DATA.getPregnancy(cow).isPresent(), "the cow's love is still held");
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
        Player owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(helper.absoluteVec(new Vec3(4.5, 1.0, 6.5)));
        Animal female = tame(female(helper, EntityTypes.WOLF), owner);
        Animal near = tame(male(helper, EntityTypes.WOLF, 5, 4, 1.0), owner);
        Animal far = tame(male(helper, EntityTypes.WOLF, 6, 6, 1.0), owner);
        female.setInLoveTime(600);

        helper.runAfterDelay(SETTLE_TICKS, () -> {
            Courtship.court(female, 0.0);
            helper.assertTrue(!Fights.fighting(near) && !Fights.fighting(far), "wolves fought over a female");
            helper.assertTrue(Courtship.chosen(female, near), "the closer wolf wasn't chosen");
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
        Player player = helper.makeMockServerPlayerInLevel();
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
