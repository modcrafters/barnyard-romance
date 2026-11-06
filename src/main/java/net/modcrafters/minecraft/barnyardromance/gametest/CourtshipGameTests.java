package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityActions;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityCare;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.rivalry.Courtship;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.modcrafters.minecraft.barnyardromance.vigour.VigourActions;
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
import java.util.UUID;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.traits;
import static net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests.holding;

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
            new BreedingGameTests.Case("pets_take_the_closest_male", 400, CourtshipGameTests::petsTakeTheClosestMale)
    );

    private CourtshipGameTests() {
    }

    public static void aFedMaleDoesNotFallInLove(GameTestHelper helper) {
        Animal bull = male(helper, EntityTypes.COW);
        FamiliarityActions.interact(bull, holding(helper, Items.WHEAT), InteractionHand.MAIN_HAND);

        helper.assertTrue(traits(helper, bull).lastFedTick() >= 0L, "the bull didn't eat");
        helper.assertTrue(!bull.isInLove(), "a fed bull fell in love");
        helper.succeed();
    }

    public static void aFemaleInLoveIsMatchedWithOneMale(GameTestHelper helper) {
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
        fenceAcrossThePen(helper);
        Animal cow = female(helper, EntityTypes.COW);
        Animal bull = fed(male(helper, EntityTypes.COW));
        cow.setInLove(null);

        helper.runAfterDelay(BREED_TICKS - 20, () -> {
            helper.assertTrue(Services.DATA.getPregnancy(cow).isEmpty(), "a bull behind a fence made the cow pregnant");
            assertVigour(helper, bull, 1.0);
            helper.succeed();
        });
    }

    public static void aMaleOnlyMatesTheFemaleHeWasChosenFor(GameTestHelper helper) {
        Animal first = female(helper, EntityTypes.COW);
        Animal second = GameTestCompat.spawn(helper, EntityTypes.COW, 3, 1, 6);
        Services.DATA.set(second, new AnimalTraits(Sex.FEMALE, 1.0, 1.0, -1L));
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
        Animal cow = female(helper, EntityTypes.COW);
        Animal bull = male(helper, EntityTypes.COW);
        bull.setHealth(2.0F);
        cow.setInLove(null);

        helper.runAfterDelay(BREED_TICKS - 20, () -> {
            helper.assertTrue(Services.DATA.getPregnancy(cow).isEmpty(), "a bull mated for his last health");
            helper.assertTrue(Math.abs(bull.getHealth() - 2.0F) < 1.0e-4F, "the bull's health is " + bull.getHealth());
            helper.succeed();
        });
    }

    public static void aHungryMalePaysHealthToMate(GameTestHelper helper) {
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
        breedsByCourtship(helper, EntityTypes.GOAT);
    }

    public static void horsesBreedByCourtship(GameTestHelper helper) {
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

    // Wolves don't fight: the closer male gets her and the other is left alone.
    public static void petsTakeTheClosestMale(GameTestHelper helper) {
        // Close by, or the wolves' follow-owner goal keeps them from their breed goal.
        Player owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(helper.absoluteVec(new Vec3(4.5, 1.0, 6.5)));
        Animal female = tame(female(helper, EntityTypes.WOLF), owner);
        Animal near = tame(fed(male(helper, EntityTypes.WOLF)), owner);
        Animal far = GameTestCompat.spawn(helper, EntityTypes.WOLF, 7, 1, 4);
        Services.DATA.set(far, new AnimalTraits(Sex.MALE, 1.0, 1.0, -1L));
        tame(fed(far), owner);
        female.setInLove(null);

        helper.succeedWhen(() -> {
            helper.assertTrue(female.isInLove() || Services.DATA.getPregnancy(female).isPresent(), "the female wolf isn't in love");
            assertFather(helper, female, near);
            assertVigour(helper, far, 1.0);
            helper.assertTrue(near.getHealth() == near.getMaxHealth() && far.getHealth() == far.getMaxHealth(), "a wolf got hurt");
        });
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

    // A fence line from wall to wall between the females (x 3) and the males (x 5).
    public static void fenceAcrossThePen(GameTestHelper helper) {
        for (int z = 0; z <= 8; z++) {
            for (int y = 1; y <= 2; y++) {
                helper.setBlock(new BlockPos(4, y, z), Blocks.OAK_FENCE.defaultBlockState());
            }
        }
    }

    public static void assertFather(GameTestHelper helper, Animal mother, Animal father) {
        Pregnancy pregnancy = Services.DATA.getPregnancy(mother).orElse(null);
        helper.assertTrue(pregnancy != null, "the " + mother.getType().getDescription().getString() + " isn't pregnant");
        UUID fatherId = pregnancy.fatherId();
        helper.assertTrue(father.getUUID().equals(fatherId), "the father is " + fatherId + ", not " + father.getUUID());
    }

    public static void assertVigour(GameTestHelper helper, Animal male, double expected) {
        double actual = VigourActions.current(male).map(value -> value.vigour().value()).orElse(-1.0);
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-6, "Vigour is " + actual + ", expected " + expected);
    }
}
