package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.core.age.GameDay;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.rivalry.Courtship;
import net.modcrafters.minecraft.barnyardromance.rivalry.Reach;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Sex;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;
import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.vigour.VigourSlices.VIGOUR;

// In-game checks for female-led breeding: only females fall in love, and each is matched with one male who can reach
// her and has the Vigour for it. Pen walls keep neighbouring tests' animals out, since a male has to walk to her.
public final class CourtshipGameTests {
    private static final int BREED_TICKS = 400;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_fed_male_does_not_fall_in_love", 20, CourtshipGameTests::aFedMaleDoesNotFallInLove),
            new BreedingGameTests.Case("a_female_in_love_is_matched_with_one_male", 400, CourtshipGameTests::aFemaleInLoveIsMatchedWithOneMale),
            new BreedingGameTests.Case("a_fence_keeps_a_male_out", 400, CourtshipGameTests::aFenceKeepsAMaleOut),
            new BreedingGameTests.Case("a_male_only_mates_the_female_he_was_chosen_for", 800, CourtshipGameTests::aMaleOnlyMatesTheFemaleHeWasChosenFor),
            new BreedingGameTests.Case("a_hungry_male_at_the_cost_is_not_chosen", 400, CourtshipGameTests::aHungryMaleAtTheCostIsNotChosen),
            new BreedingGameTests.Case("a_hungry_male_pays_health_to_mate", 400, CourtshipGameTests::aHungryMalePaysHealthToMate),
            new BreedingGameTests.Case("goats_breed_by_courtship", 400, CourtshipGameTests::goatsBreedByCourtship),
            new BreedingGameTests.Case("horses_breed_by_courtship", 400, CourtshipGameTests::horsesBreedByCourtship),
            new BreedingGameTests.Case("pets_take_the_closest_male", 400, CourtshipGameTests::petsTakeTheClosestMale),
            new BreedingGameTests.Case("reach_ignores_where_the_male_is_walking", 100, CourtshipGameTests::reachIgnoresWhereTheMaleIsWalking),
            new BreedingGameTests.Case("a_chosen_male_walks_to_a_far_female", 400, CourtshipGameTests::aChosenMaleWalksToAFarFemale)
    );

    private CourtshipGameTests() {
    }

    public static void aFedMaleDoesNotFallInLove(GameTestHelper helper) {
        clearOfMidnight(helper, 20);
        Animal bull = male(helper, EntityTypes.COW);
        FamiliarityActions.interact(bull, holding(helper, Items.WHEAT), InteractionHand.MAIN_HAND);

        helper.assertTrue(traits(helper, bull).get(CARE).lastFedTick() >= 0L, "the bull didn't eat");
        helper.assertTrue(!bull.isInLove(), "a fed bull fell in love");
        helper.succeed();
    }

    public static void aFemaleInLoveIsMatchedWithOneMale(GameTestHelper helper) {
        clearOfMidnight(helper, 400);
        Animal cow = female(helper, EntityTypes.COW);
        Animal bull = fed(male(helper, EntityTypes.COW));
        cow.setInLove(null);

        helper.succeedWhen(() -> {
            assertFather(helper, cow, bull);
            assertVigour(helper, bull, 0.7);
            helper.assertTrue(bull.getAge() == 0, "the bull has a breeding rest of " + bull.getAge());
        });
    }

    public static void aFenceKeepsAMaleOut(GameTestHelper helper) {
        clearOfMidnight(helper, 400);
        Animal cow = female(helper, EntityTypes.COW);
        Animal bull = fed(male(helper, EntityTypes.COW));
        EggGameTests.once(helper, bull::onGround, () -> {
            fenceAround(helper, bull);
            cow.setInLove(null);
        });

        helper.runAfterDelay(BREED_TICKS - 20, () -> {
            helper.assertTrue(Services.DATA.get(cow, PregnancyFeature.PREGNANCY).isEmpty(), "a bull behind a fence made the cow pregnant: the bull at "
                    + helper.relativePos(bull.blockPosition()) + ", the cow at " + helper.relativePos(cow.blockPosition()));
            assertVigour(helper, bull, 1.0);
            helper.succeed();
        });
    }

    public static void aMaleOnlyMatesTheFemaleHeWasChosenFor(GameTestHelper helper) {
        clearOfMidnight(helper, 800);
        Animal first = female(helper, EntityTypes.COW);
        Animal second = GameTestCompat.spawn(helper, EntityTypes.COW, 3, 1, 6);
        Services.DATA.set(second, AnimalTraits.of(Sex.FEMALE, 1.0).with(CARE, FamiliarityState.NONE.fed(1.0, -1L)));
        Animal bull = fed(male(helper, EntityTypes.COW));
        first.setInLove(null);
        second.setInLove(null);

        // Paths can't be found on the tick the animals appear, so the bull may be chosen a second later.
        helper.onEachTick(() -> {
            if (Courtship.chosen(first, bull)) {
                helper.assertTrue(!second.canMate(bull) && !bull.canMate(second), "the second cow can mate a bull chosen for the first");
            }
            if (Courtship.chosen(second, bull)) {
                helper.assertTrue(!first.canMate(bull), "the first cow can mate a bull chosen for the second");
            }
        });
        helper.succeedWhen(() -> {
            assertFather(helper, first, bull);
            assertFather(helper, second, bull);
            assertVigour(helper, bull, 0.4);
        });
    }

    // Mating would cost him his last 2 health.
    public static void aHungryMaleAtTheCostIsNotChosen(GameTestHelper helper) {
        clearOfMidnight(helper, 400);
        Animal cow = female(helper, EntityTypes.COW);
        Animal bull = male(helper, EntityTypes.COW);
        bull.setHealth(2.0F);
        cow.setInLove(null);

        helper.runAfterDelay(BREED_TICKS - 20, () -> {
            helper.assertTrue(Services.DATA.get(cow, PregnancyFeature.PREGNANCY).isEmpty(), "a bull mated for his last health");
            helper.assertTrue(Math.abs(bull.getHealth() - 2.0F) < 1.0e-4F, "the bull's health is " + bull.getHealth());
            helper.succeed();
        });
    }

    public static void aHungryMalePaysHealthToMate(GameTestHelper helper) {
        clearOfMidnight(helper, 400);
        Animal cow = female(helper, EntityTypes.COW);
        Animal bull = male(helper, EntityTypes.COW);
        bull.setHealth(3.0F);
        helper.assertTrue(VigourActions.hungry(bull), "a never-fed bull isn't hungry");
        cow.setInLove(null);

        helper.succeedWhen(() -> {
            assertFather(helper, cow, bull);
            helper.assertTrue(Math.abs(bull.getHealth() - 1.0F) < 1.0e-4F, "the bull's health is " + bull.getHealth());
        });
    }

    public static void goatsBreedByCourtship(GameTestHelper helper) {
        clearOfMidnight(helper, 400);
        breedsByCourtship(helper, EntityTypes.GOAT);
    }

    public static void horsesBreedByCourtship(GameTestHelper helper) {
        clearOfMidnight(helper, 400);
        Animal mare = female(helper, EntityTypes.HORSE);
        Animal stallion = fed(male(helper, EntityTypes.HORSE));
        ((AbstractHorse) mare).setTamed(true);
        ((AbstractHorse) stallion).setTamed(true);
        mare.setInLove(null);

        helper.succeedWhen(() -> {
            assertFather(helper, mare, stallion);
            assertVigour(helper, stallion, 0.7);
        });
    }

    // Wolves don't fight: the closer male gets her and the other is left alone. She falls in love only once all three
    // have landed, so the choice is made there and then, with every path open, rather than at a later look whose tick
    // depends on entity ids while the wolves wander. Every tick after, the far wolf must not be chosen; if he is, the
    // failure says what the near one was doing at that moment.
    public static void petsTakeTheClosestMale(GameTestHelper helper) {
        clearOfMidnight(helper, 400);
        // Close by, or the wolves' follow-owner goal keeps them from their breed goal.
        Player owner = MockPlayers.inLevel(helper);
        owner.setPos(helper.absoluteVec(new Vec3(4.5, 1.0, 6.5)));
        Animal female = tame(female(helper, EntityTypes.WOLF), owner);
        Animal near = tame(fed(male(helper, EntityTypes.WOLF)), owner);
        Animal far = GameTestCompat.spawn(helper, EntityTypes.WOLF, 7, 1, 4);
        Services.DATA.set(far, AnimalTraits.of(Sex.MALE, 1.0).with(CARE, FamiliarityState.NONE.fed(1.0, -1L)));
        tame(fed(far), owner);
        boolean[] inLove = {false};

        EggGameTests.once(helper, () -> female.onGround() && near.onGround() && far.onGround(), () -> {
            female.setInLove(null);
            inLove[0] = true;
            helper.assertTrue(Courtship.chosen(female, near), "the near wolf wasn't chosen the moment she fell in love: near "
                    + blocks(near, female) + " blocks off, far " + blocks(far, female) + ", far chosen " + Courtship.chosen(female, far));
        });
        helper.onEachTick(() -> helper.assertTrue(!(inLove[0] && Courtship.chosen(female, far)), "the far wolf was chosen instead: near "
                + blocks(near, female) + " blocks off, alive " + near.isAlive() + ", in love " + near.isInLove() + "; far " + blocks(far, female) + " blocks off"));

        helper.succeedWhen(() -> {
            helper.assertTrue(inLove[0], "the wolves haven't landed");
            assertFather(helper, female, near);
            assertVigour(helper, far, 1.0);
            helper.assertTrue(near.getHealth() == near.getMaxHealth() && far.getHealth() == far.getMaxHealth(), "a wolf got hurt");
        });
    }

    private static String blocks(Animal male, Animal female) {
        return String.format(Locale.ROOT, "%.1f", male.distanceTo(female));
    }

    // Vanilla's path search hands back the path a mob is already walking when asked about the same target again, so a
    // second look must not mistake his own walk for the way to her.
    public static void reachIgnoresWhereTheMaleIsWalking(GameTestHelper helper) {
        clearOfMidnight(helper, 100);
        Animal cow = female(helper, EntityTypes.COW);
        Animal bull = male(helper, EntityTypes.COW);
        EggGameTests.once(helper, () -> bull.onGround() && cow.onGround(), () -> {
            cow.setNoAi(true);
            Vec3 corner = helper.absoluteVec(new Vec3(7.5, 1.0, 7.5));
            bull.getNavigation().moveTo(corner.x, bull.getY(), corner.z, 1.0);
            boolean first = Reach.canWalk(bull, cow, 16);
            boolean second = Reach.canWalk(bull, cow, 16);
            helper.assertTrue(first && second, "the bull can walk to the cow: " + first + ", then " + second);
            helper.succeed();
        });
    }

    // Vanilla's breed goal only looks 8 blocks for a partner, but a male up to matingRange (16) away can be chosen, so he
    // has to come to her. She stands still here, and he starts across the pen, more than 8 blocks off.
    public static void aChosenMaleWalksToAFarFemale(GameTestHelper helper) {
        clearOfMidnight(helper, 400);
        Animal cow = GameTestCompat.spawn(helper, EntityTypes.COW, 1, 1, 1);
        Services.DATA.set(cow, AnimalTraits.of(Sex.FEMALE, 1.0).with(CARE, FamiliarityState.NONE.fed(1.0, -1L)));
        Animal bull = GameTestCompat.spawn(helper, EntityTypes.COW, 7, 1, 7);
        Services.DATA.set(bull, AnimalTraits.of(Sex.MALE, 1.0).with(CARE, FamiliarityState.NONE.fed(1.0, -1L)));
        fed(bull);
        // Frozen once she's down: a mob without AI never lands.
        EggGameTests.once(helper, () -> bull.onGround() && cow.onGround(), () -> {
            cow.setNoAi(true);
            helper.assertTrue(bull.distanceTo(cow) > 8.0, "the bull starts only " + bull.distanceTo(cow) + " blocks off");
            cow.setInLove(null);
        });

        helper.succeedWhen(() -> assertFather(helper, cow, bull));
    }

    private static void breedsByCourtship(GameTestHelper helper, EntityType<? extends Animal> type) {
        Animal mother = female(helper, type);
        Animal father = fed(male(helper, type));
        mother.setInLove(null);

        helper.succeedWhen(() -> {
            assertFather(helper, mother, father);
            assertVigour(helper, father, 0.7);
        });
    }

    public static Animal fed(Animal male) {
        FamiliarityCare.markEaten(male);
        return male;
    }

    // Owned and standing: an ownerless tamed wolf always sits, and vanilla never lets a sitting wolf mate.
    private static Animal tame(Animal wolf, Player owner) {
        TamableAnimal pet = (TamableAnimal) wolf;
        pet.tame(owner);
        pet.setOrderedToSit(false);
        pet.setInSittingPose(false);
        return wolf;
    }

    // A fence cell two high around the block the animal stands in, so it can't walk out. Call it once the animal has
    // landed: where a test pen's floor is differs between versions, so the animal is the only sure guide.
    public static void fenceAround(GameTestHelper helper, Animal animal) {
        BlockPos at = animal.blockPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    helper.getLevel().setBlockAndUpdate(at.offset(dx, 0, dz), Blocks.OAK_FENCE.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(at.offset(dx, 1, dz), Blocks.OAK_FENCE.defaultBlockState());
                }
            }
        }
    }

    // A day ending mid-test settles it: love ends, and an unfed animal loses familiarity and health. These tests run on
    // the live overworld clock, where the suite before them leaves it anywhere, so one that would see midnight before its
    // time is up starts at the next sunrise instead, before its animals are made.
    static void clearOfMidnight(GameTestHelper helper, int ticks) {
        long clock = helper.getLevel().getOverworldClockTime();
        long untilMidnight = GameDay.ticksUntilMidnight(clock);
        if (untilMidnight <= ticks) {
            GameTestCompat.setOverworldClock(helper, clock + untilMidnight + GameDay.TICKS - 18_000L);
        }
    }

    public static void assertFather(GameTestHelper helper, Animal mother, Animal father) {
        Pregnancy pregnancy = Services.DATA.get(mother, PregnancyFeature.PREGNANCY).orElse(null);
        helper.assertTrue(pregnancy != null, "the " + mother.getType().getDescription().getString() + " isn't pregnant");
        UUID fatherId = pregnancy.fatherId();
        helper.assertTrue(father.getUUID().equals(fatherId), "the father is " + fatherId + ", not " + father.getUUID());
    }

    public static void assertVigour(GameTestHelper helper, Animal male, double expected) {
        double actual = VigourActions.current(male).map(value -> value.get(VIGOUR).value()).orElse(-1.0);
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-6, "Vigour is " + actual + ", expected " + expected);
    }
}
