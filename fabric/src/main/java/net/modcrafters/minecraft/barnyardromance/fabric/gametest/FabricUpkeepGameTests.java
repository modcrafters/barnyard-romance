package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.UpkeepGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricUpkeepGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void aHungryFamiliarCowLosesHalfAHeart(GameTestHelper helper) {
        UpkeepGameTests.aHungryFamiliarCowLosesHalfAHeart(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungerStopsAtMinHealth(GameTestHelper helper) {
        UpkeepGameTests.hungerStopsAtMinHealth(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void minHealthZeroStarves(GameTestHelper helper) {
        UpkeepGameTests.minHealthZeroStarves(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWildCowIsNeverHurt(GameTestHelper helper) {
        UpkeepGameTests.aWildCowIsNeverHurt(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void oneDayModeCountsAWeekAsADay(GameTestHelper helper) {
        UpkeepGameTests.oneDayModeCountsAWeekAsADay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void noneModeIgnoresMissedDays(GameTestHelper helper) {
        UpkeepGameTests.noneModeIgnoresMissedDays(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void reachingTheThresholdMarksHerCaredFor(GameTestHelper helper) {
        UpkeepGameTests.reachingTheThresholdMarksHerCaredFor(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anOldSaveHerdBookCowCountsAsCaredFor(GameTestHelper helper) {
        UpkeepGameTests.anOldSaveHerdBookCowCountsAsCaredFor(helper);
    }
}
