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

    @GameTest(structure = PEN, maxTicks = 20)
    public void aFarmBornCalfInABookIsNotCaredFor(GameTestHelper helper) {
        UpkeepGameTests.aFarmBornCalfInABookIsNotCaredFor(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungerKeepsASittingWolfSitting(GameTestHelper helper) {
        UpkeepGameTests.hungerKeepsASittingWolfSitting(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void noneModeStillCountsLastNight(GameTestHelper helper) {
        UpkeepGameTests.noneModeStillCountsLastNight(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungryDaysStopHurtingBelowFamiliar(GameTestHelper helper) {
        UpkeepGameTests.hungryDaysStopHurtingBelowFamiliar(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void minHealthClampsPartway(GameTestHelper helper) {
        UpkeepGameTests.minHealthClampsPartway(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aPennedRunawayEndsUpOutside(GameTestHelper helper) {
        UpkeepGameTests.aPennedRunawayEndsUpOutside(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aLeapingRunawayClearsTheFence(GameTestHelper helper) {
        UpkeepGameTests.aLeapingRunawayClearsTheFence(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aRunawayNeverRollsAgain(GameTestHelper helper) {
        UpkeepGameTests.aRunawayNeverRollsAgain(helper);
    }
}
