package net.modcrafters.minecraft.barnyardromance.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.Animal;
import net.modcrafters.minecraft.barnyardromance.core.animal.Tracked;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.pregnancy.PregnancyFeature;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.female;
import static net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests.male;

// Which animals the mod tracks out of the box, and what happens to one that stops being tracked.
public final class TrackingGameTests {
    private static final String FOXES = "{\"species\": {\"minecraft:fox\": {\"enabled\": true}}}";
    private static final int WAIT_TICKS = 200;

    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_fox_and_a_panda_are_untracked", 20, TrackingGameTests::aFoxAndAPandaAreUntracked),
            new BreedingGameTests.Case("a_pregnant_fox_left_untracked_is_harmless", WAIT_TICKS + 20, TrackingGameTests::aPregnantFoxLeftUntrackedIsHarmless)
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
}
