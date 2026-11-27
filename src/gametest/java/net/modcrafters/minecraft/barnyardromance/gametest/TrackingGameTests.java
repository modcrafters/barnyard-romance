package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.api.Sex;
import net.modcrafters.minecraft.barnyardromance.core.AnimalTicks;
import net.modcrafters.minecraft.barnyardromance.core.animal.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.FeaturesRepository;
import net.modcrafters.minecraft.barnyardromance.eggs.EggsFeature;
import net.modcrafters.minecraft.barnyardromance.eggs.api.EggsApi;
import net.modcrafters.minecraft.barnyardromance.eggs.api.Laying;
import net.modcrafters.minecraft.barnyardromance.familiarity.FamiliarityState;
import net.modcrafters.minecraft.barnyardromance.pregnancy.api.PregnancyApi;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyHearts;
import net.modcrafters.minecraft.barnyardromance.stats.LiveFacts;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.familiarity.FamiliaritySlices.CARE;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;

// Which animals the mod tracks out of the box, and what happens to one that stops being tracked.
public final class TrackingGameTests {
    private static final String FOXES = "{\"species\": {\"minecraft:fox\": {\"enabled\": true}}}";
    private static final String NO_CHICKENS = "{\"species\": {\"minecraft:chicken\": {\"enabled\": false}}}";
    private static final int WAIT_TICKS = 200;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_fox_and_a_panda_are_untracked", 20, TrackingGameTests::aFoxAndAPandaAreUntracked),
            new BreedingGameTests.Case("a_pregnant_fox_left_untracked_is_harmless", WAIT_TICKS + 20, TrackingGameTests::aPregnantFoxLeftUntrackedIsHarmless),
            new BreedingGameTests.Case("untracked_foxes_breed_as_vanilla", 20,TrackingGameTests::untrackedFoxesBreedAsVanilla),
            new BreedingGameTests.Case("an_untracked_fox_shows_no_stats", 20, TrackingGameTests::anUntrackedFoxShowsNoStats),
            new BreedingGameTests.Case("an_untracked_fox_goes_back_to_its_vanilla_size", 20, TrackingGameTests::anUntrackedFoxGoesBackToItsVanillaSize),
            new BreedingGameTests.Case("the_api_does_not_report_an_untracked_fox_pregnant", 20, TrackingGameTests::theApiDoesNotReportAnUntrackedFoxPregnant),
            new BreedingGameTests.Case("the_api_does_not_report_an_untracked_hens_laying", 20, TrackingGameTests::theApiDoesNotReportAnUntrackedHensLaying)
    );

    private TrackingGameTests() {
    }

    public static void aFoxAndAPandaAreUntracked(GameTestHelper helper) {
        Animal fox = GameTestCompat.spawn(helper, EntityTypes.FOX, 3, 1, 4);
        Animal panda = GameTestCompat.spawn(helper, EntityTypes.PANDA, 5, 1, 4);

        helper.assertTrue(Services.DATA.get(fox).isEmpty(), "a fox has traits");
        helper.assertTrue(Services.DATA.get(panda).isEmpty(), "a panda has traits");
        helper.succeed();
    }

    // A pack that switches foxes on and later off again, or an update that drops them, must leave the mother alone:
    // no crash, no stuck belly, and what the mod saved stays on her.
    public static void aPregnantFoxLeftUntrackedIsHarmless(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.FOX);
        Animal father = male(helper, EntityTypes.FOX);
        UpkeepGameTests.withConfig(FOXES, () -> {
            mother.setInLove(null);
            father.setInLove(null);
            mother.spawnChildFromBreeding(helper.getLevel(), father);
            helper.assertTrue(Services.DATA.get(mother, PregnancyFeature.PREGNANCY).isPresent(), "the fox didn't become pregnant, so this proves nothing");
        });

        helper.runAfterDelay(WAIT_TICKS, () -> {
            helper.assertTrue(mother.isAlive(), "the fox died");
            helper.assertTrue(Tracked.traits(mother).isEmpty(), "the untracked fox is still tracked");
            helper.assertTrue(Services.DATA.get(mother).isPresent(), "the fox lost its traits");
            helper.succeed();
        });
    }

    // Foxes that had traits while a pack tracked them, one of them pregnant, breed by vanilla's rules once foxes are
    // untracked again: a vixen and a dog fox can mate, the pregnant one too, and two of the same sex are left to
    // vanilla, which only asks that both are in love.
    public static void untrackedFoxesBreedAsVanilla(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.FOX);
        Animal father = male(helper, EntityTypes.FOX);
        Animal sister = withTraits(GameTestCompat.spawn(helper, EntityTypes.FOX, 4, 1, 3), Sex.FEMALE);
        Animal brother = withTraits(GameTestCompat.spawn(helper, EntityTypes.FOX, 4, 1, 5), Sex.MALE);
        UpkeepGameTests.withConfig(FOXES, () -> {
            mother.setInLove(null);
            father.setInLove(null);
            mother.spawnChildFromBreeding(helper.getLevel(), father);
            helper.assertTrue(Services.DATA.get(mother, PregnancyFeature.PREGNANCY).isPresent(), "the fox didn't become pregnant, so this proves nothing");
        });

        for (Animal fox : List.of(mother, father, sister, brother)) {
            fox.setInLove(null);
            helper.assertTrue(fox.isInLove(), "an untracked fox didn't fall in love");
        }
        assertVanillaMating(helper, mother, father, sister, brother);
        // The pregnancy left on her doesn't take her love away on the next tick either. (Vanilla's own breeding rest
        // would, so the hearts tick is run directly.)
        PregnancyHearts.serverTick(mother, helper.getLevel());
        helper.assertTrue(mother.isInLove(), "the untracked pregnant fox's love was taken away");
        assertVanillaMating(helper, mother, father, sister, brother);
        helper.succeed();
    }

    private static Animal withTraits(Animal animal, Sex sex) {
        Services.DATA.set(animal, AnimalTraits.of(sex, 1.0).with(CARE, FamiliarityState.NONE.fed(1.0, -1L)));
        return animal;
    }

    private static void assertVanillaMating(GameTestHelper helper, Animal mother, Animal father, Animal sister, Animal brother) {
        helper.assertTrue(mother.canMate(father), "the untracked pregnant vixen can't mate");
        helper.assertTrue(father.canMate(mother), "the untracked dog fox can't mate with the pregnant vixen");
        helper.assertTrue(sister.canMate(brother) && brother.canMate(sister), "an untracked vixen and dog fox can't mate");
        helper.assertTrue(sister.canMate(mother) && mother.canMate(sister), "two untracked vixens don't follow vanilla, which lets them mate");
        helper.assertTrue(father.canMate(brother), "two untracked dog foxes don't follow vanilla, which lets them mate");
    }

    // What the mod saved on a fox while it was tracked isn't shown once foxes are untracked.
    public static void anUntrackedFoxShowsNoStats(GameTestHelper helper) {
        Animal fox = female(helper, EntityTypes.FOX);
        long now = helper.getLevel().getGameTime();
        long clock = helper.getLevel().getOverworldClockTime();
        UpkeepGameTests.withConfig(FOXES, () ->
                helper.assertTrue(LiveFacts.of(fox, now, clock).isPresent(), "a tracked fox shows no stats, so this proves nothing"));

        helper.assertTrue(LiveFacts.of(fox, now, clock).isEmpty(), "an untracked fox still shows its stats");
        helper.succeed();
    }

    // The API reports what the mod looks after: a pregnancy saved on a fox that is untracked now isn't one, and comes back
    // when foxes are switched on again.
    public static void theApiDoesNotReportAnUntrackedFoxPregnant(GameTestHelper helper) {
        Animal mother = female(helper, EntityTypes.FOX);
        Animal father = male(helper, EntityTypes.FOX);
        PregnancyApi api = FeaturesRepository.get(PregnancyApi.class);
        UpkeepGameTests.withConfig(FOXES, () -> {
            mother.setInLove(null);
            father.setInLove(null);
            mother.spawnChildFromBreeding(helper.getLevel(), father);
            helper.assertTrue(api.isPregnant(mother) && api.pregnancy(mother).isPresent(), "the tracked fox isn't pregnant, so this proves nothing");
        });
        helper.assertTrue(Services.DATA.get(mother, PregnancyFeature.PREGNANCY).isPresent(), "the pregnancy wasn't left on the fox");

        helper.assertTrue(!api.isPregnant(mother), "the API calls an untracked fox pregnant");
        helper.assertTrue(api.pregnancy(mother).isEmpty(), "the API gives an untracked fox's pregnancy");

        UpkeepGameTests.withConfig(FOXES, () ->
                helper.assertTrue(api.isPregnant(mother) && api.pregnancy(mother).isPresent(), "switching foxes back on doesn't bring the pregnancy back"));
        helper.succeed();
    }

    // The same for the laying a hen saved while chickens were tracked.
    public static void theApiDoesNotReportAnUntrackedHensLaying(GameTestHelper helper) {
        Animal hen = EggGameTests.chicken(helper, Sex.FEMALE, EggGameTests.mediumHen(), 0.5, 3);
        Services.DATA.set(hen, EggsFeature.LAYING, new Laying(1L, List.of(5_000), 1, 1));
        EggsApi api = FeaturesRepository.get(EggsApi.class);
        helper.assertTrue(api.laying(hen).isPresent(), "the tracked hen has no laying, so this proves nothing");

        UpkeepGameTests.withConfig(NO_CHICKENS, () ->
                helper.assertTrue(api.laying(hen).isEmpty(), "the API gives an untracked hen's laying"));
        helper.succeed();
    }

    // A fox sized while foxes were tracked is vanilla-sized again once they aren't, keeps its traits, and is sized again
    // when foxes are switched back on. A fox another mod scaled keeps that mod's scale.
    public static void anUntrackedFoxGoesBackToItsVanillaSize(GameTestHelper helper) {
        Animal fox = GameTestCompat.spawn(helper, EntityTypes.FOX, 3, 1, 4);
        Animal scaledElsewhere = GameTestCompat.spawn(helper, EntityTypes.FOX, 5, 1, 4);
        Services.DATA.set(fox, AnimalTraits.of(Sex.MALE, 1.1));
        Services.DATA.set(scaledElsewhere, AnimalTraits.of(Sex.MALE, 1.1));
        UpkeepGameTests.withConfig(FOXES, () -> {
            AnimalTicks.joined(fox);
            AnimalTicks.joined(scaledElsewhere);
        });
        double sized = Services.DATA.get(fox).orElseThrow().scale();
        helper.assertTrue(Math.abs(sized - 1.0) > 0.01, "the fox wasn't sized, so this proves nothing");
        assertScale(helper, fox, sized);
        scale(helper, scaledElsewhere).setBaseValue(0.5);

        AnimalTicks.joined(fox);
        AnimalTicks.joined(scaledElsewhere);

        assertScale(helper, fox, 1.0);
        helper.assertTrue(Services.DATA.get(fox).isPresent(), "the untracked fox lost its traits");
        assertScale(helper, scaledElsewhere, 0.5);

        UpkeepGameTests.withConfig(FOXES, () -> AnimalTicks.joined(fox));
        assertScale(helper, fox, sized);
        helper.succeed();
    }

    private static AttributeInstance scale(GameTestHelper helper, Animal animal) {
        AttributeInstance scale = GameTestCompat.scaleAttribute(animal);
        helper.assertTrue(scale != null, "a fox has no scale");
        return scale;
    }

    private static void assertScale(GameTestHelper helper, Animal animal, double expected) {
        double actual = scale(helper, animal).getBaseValue();
        helper.assertTrue(Math.abs(actual - expected) < 1.0e-6, "the fox's scale is " + actual + ", not " + expected);
    }
}
