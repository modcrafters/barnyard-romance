package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.Constants;
import net.modcrafters.minecraft.barnyardromance.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.Pregnancy;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;

import java.util.List;
import java.util.function.Consumer;

// In-game checks for breeding, run headless by each loader's game test server. Only registered when game tests are on.
public final class BreedingGameTests {
    public static final Identifier PEN = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "gametest/pen");
    public static final int VANILLA_REST = 6000;

    public record Case(String name, int maxTicks, Consumer<GameTestHelper> body) {
    }

    public static final List<Case> CASES = List.of(
            new Case("pigs_conceive_a_litter_and_rest", 200, BreedingGameTests::pigsConceiveALitterAndRest),
            new Case("cows_carry_one_calf_for_about_three_days", 20, BreedingGameTests::cowsCarryOneCalfForAboutThreeDays),
            new Case("same_sex_pairs_make_nothing", 20, BreedingGameTests::sameSexPairsMakeNothing),
            new Case("unfamiliar_animals_do_not_fall_in_love", 20, BreedingGameTests::unfamiliarAnimalsDoNotFallInLove),
            new Case("hurting_an_animal_ends_its_love", 20, BreedingGameTests::hurtingAnAnimalEndsItsLove),
            new Case("pigs_in_love_breed_on_their_own", 400, BreedingGameTests::pigsInLoveBreedOnTheirOwn)
    );

    private BreedingGameTests() {
    }

    public static void pigsConceiveALitterAndRest(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.PIG);
        Animal father = male(helper, EntityTypes.PIG);
        long now = helper.getLevel().getGameTime();
        mother.setInLove(null);
        father.setInLove(null);
        helper.assertTrue(traits(helper, mother).loveUntil() == now + mother.getInLoveTime(), "love mode was not recorded on the mother");

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        Pregnancy pregnancy = pregnancy(helper, mother);
        helper.assertTrue(pregnancy.litterSize() >= 2 && pregnancy.litterSize() <= 4, "pig litter " + pregnancy.litterSize() + " is outside 2-4");
        helper.assertTrue(pregnancy.durationTicks() >= 30_000 && pregnancy.durationTicks() <= 36_000,
                "pig pregnancy " + pregnancy.durationTicks() + " is outside 30000-36000");
        helper.assertTrue(pregnancy.litter().size() == pregnancy.litterSize(), "the stored litter does not match the litter size");
        helper.assertTrue(Services.DATA.getPregnancy(father).isEmpty(), "the father became pregnant");
        assertResting(helper, mother, now);
        assertResting(helper, father, now);
        helper.assertTrue(!helper.getEntities(EntityTypes.EXPERIENCE_ORB).isEmpty(), "breeding gave no XP");

        int litterSize = pregnancy.litterSize();
        Services.DATA.setPregnancy(mother, pregnancy.withConceivedAt(now - pregnancy.durationTicks()));
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.getPregnancy(mother).isEmpty(), "the mother is still pregnant");
            List<? extends Animal> babies = helper.getEntities(EntityTypes.PIG).stream().filter(Animal::isBaby).toList();
            helper.assertTrue(babies.size() == litterSize, "expected " + litterSize + " piglets, found " + babies.size());
            for (Animal baby : babies) {
                helper.assertTrue(Services.DATA.get(baby).isPresent(), "a piglet was born without traits");
            }
        });
    }

    public static void cowsCarryOneCalfForAboutThreeDays(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.COW);
        Animal father = male(helper, EntityTypes.COW);
        mother.setInLove(null);
        father.setInLove(null);

        mother.spawnChildFromBreeding(helper.getLevel(), father);

        Pregnancy pregnancy = pregnancy(helper, mother);
        helper.assertTrue(pregnancy.litterSize() == 1, "cow litter " + pregnancy.litterSize() + " is not 1");
        helper.assertTrue(pregnancy.durationTicks() >= 60_000 && pregnancy.durationTicks() <= 72_000,
                "cow pregnancy " + pregnancy.durationTicks() + " is outside 60000-72000");
        helper.succeed();
    }

    public static void sameSexPairsMakeNothing(GameTestHelper helper) {
        Animal first = male(helper, EntityTypes.COW);
        Animal second = male(helper, EntityTypes.COW);
        first.setInLove(null);
        second.setInLove(null);

        first.spawnChildFromBreeding(helper.getLevel(), second);

        helper.assertTrue(Services.DATA.getPregnancy(first).isEmpty() && Services.DATA.getPregnancy(second).isEmpty(), "a same-sex pair conceived");
        helper.assertTrue(!first.isInLove() && !second.isInLove(), "a same-sex pair stayed in love");
        helper.assertTrue(traits(helper, first).loveUntil() < 0L && traits(helper, second).loveUntil() < 0L, "love was not cleared on a same-sex pair");
        helper.assertTrue(helper.getEntities(EntityTypes.EXPERIENCE_ORB).isEmpty(), "a same-sex pair gave XP");
        helper.succeed();
    }

    public static void unfamiliarAnimalsDoNotFallInLove(GameTestHelper helper) {
        Animal cow = helper.spawn(EntityTypes.COW, 4, 1, 4);
        Services.DATA.set(cow, new AnimalTraits(Sex.FEMALE, 1.0, 0.0, -1L));

        cow.setInLove(null);

        helper.assertTrue(!cow.isInLove(), "an unfamiliar cow fell in love");
        helper.assertTrue(traits(helper, cow).loveUntil() < 0L, "love was recorded on an unfamiliar cow");
        helper.succeed();
    }

    public static void hurtingAnAnimalEndsItsLove(GameTestHelper helper) {
        Animal cow = female(helper, EntityTypes.COW);
        cow.setInLove(null);
        helper.assertTrue(traits(helper, cow).loveUntil() > helper.getLevel().getGameTime(), "love mode was not recorded");

        cow.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1.0F);

        helper.assertTrue(traits(helper, cow).loveUntil() < 0L, "love was not cleared after the cow was hurt");
        helper.succeed();
    }

    public static void pigsInLoveBreedOnTheirOwn(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.PIG);
        Animal father = male(helper, EntityTypes.PIG);
        mother.setInLove(null);
        father.setInLove(null);
        helper.succeedWhen(() -> {
            helper.assertTrue(Services.DATA.getPregnancy(mother).isPresent(), "the pigs have not bred yet");
            helper.assertTrue(traits(helper, mother).cooldownUntil() > helper.getLevel().getGameTime(), "the mother is not resting");
        });
    }

    public static Animal female(GameTestHelper helper, EntityType<? extends Animal> type) {
        Animal animal = helper.spawn(type, 3, 1, 4);
        Services.DATA.set(animal, new AnimalTraits(Sex.FEMALE, 1.0, 1.0, -1L));
        return animal;
    }

    public static Animal male(GameTestHelper helper, EntityType<? extends Animal> type) {
        Animal animal = helper.spawn(type, 5, 1, 4);
        Services.DATA.set(animal, new AnimalTraits(Sex.MALE, 1.0, 1.0, -1L));
        return animal;
    }

    public static AnimalTraits traits(GameTestHelper helper, Animal animal) {
        return Services.DATA.get(animal).orElseThrow(() -> helper.assertionException(Component.literal(animal.getType() + " has no traits")));
    }

    public static Pregnancy pregnancy(GameTestHelper helper, Animal mother) {
        return Services.DATA.getPregnancy(mother).orElseThrow(() -> helper.assertionException(Component.literal("the mother did not become pregnant")));
    }

    // Vanilla's rest after breeding: the age cooldown, love cleared, and both mirrored into the synced traits.
    public static void assertResting(GameTestHelper helper, Animal parent, long now) {
        helper.assertTrue(parent.getAge() == VANILLA_REST, "a parent's age is " + parent.getAge() + ", not " + VANILLA_REST);
        helper.assertTrue(!parent.isInLove(), "a parent is still in love");
        AnimalTraits traits = traits(helper, parent);
        helper.assertTrue(traits.loveUntil() < 0L, "a parent still shows love");
        helper.assertTrue(traits.cooldownUntil() == now + VANILLA_REST, "a parent rests until " + traits.cooldownUntil() + ", not " + (now + VANILLA_REST));
    }
}
