package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.CareGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricCareGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void fedCowKeepsFamiliarityOverDays(GameTestHelper helper) {
        CareGameTests.fedCowKeepsFamiliarityOverDays(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungryCowLosesFivePercentADay(GameTestHelper helper) {
        CareGameTests.hungryCowLosesFivePercentADay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void feedingMarksTheDayEaten(GameTestHelper helper) {
        CareGameTests.feedingMarksTheDayEaten(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void superfoodCountsAsEating(GameTestHelper helper) {
        CareGameTests.superfoodCountsAsEating(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void calfDoesNotGoHungry(GameTestHelper helper) {
        CareGameTests.calfDoesNotGoHungry(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void oldTamedWolfStartsAtThreshold(GameTestHelper helper) {
        CareGameTests.oldTamedWolfStartsAtThreshold(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void horseTamedByRidingIsNotGrumpy(GameTestHelper helper) {
        CareGameTests.horseTamedByRidingIsNotGrumpy(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void unloadedMemberShowsProjectedFamiliarity(GameTestHelper helper) {
        CareGameTests.unloadedMemberShowsProjectedFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void milkingCostsFamiliarity(GameTestHelper helper) {
        CareGameTests.milkingCostsFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void largeCowMilkedTwiceCostsTen(GameTestHelper helper) {
        CareGameTests.largeCowMilkedTwiceCostsTen(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void refusedMilkingCostsNothing(GameTestHelper helper) {
        CareGameTests.refusedMilkingCostsNothing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void shearingCostsFamiliarity(GameTestHelper helper) {
        CareGameTests.shearingCostsFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void dispenserShearingCostsFamiliarity(GameTestHelper helper) {
        CareGameTests.dispenserShearingCostsFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void mooshroomStewCostsFamiliarity(GameTestHelper helper) {
        CareGameTests.mooshroomStewCostsFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungryCowGivesNoMilkNextDay(GameTestHelper helper) {
        CareGameTests.hungryCowGivesNoMilkNextDay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungryDayDoesNotDryHerUp(GameTestHelper helper) {
        CareGameTests.hungryDayDoesNotDryHerUp(helper);
    }
}
