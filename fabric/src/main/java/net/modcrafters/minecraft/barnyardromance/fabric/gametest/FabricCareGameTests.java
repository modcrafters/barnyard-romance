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
    public void oldTamedWolfGetsTamingGrace(GameTestHelper helper) {
        CareGameTests.oldTamedWolfGetsTamingGrace(helper);
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
    public void hungryDaysCountTowardsDryingUp(GameTestHelper helper) {
        CareGameTests.hungryDaysCountTowardsDryingUp(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void grumpyHorseRefusesRider(GameTestHelper helper) {
        CareGameTests.grumpyHorseRefusesRider(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void contentHorseTakesRider(GameTestHelper helper) {
        CareGameTests.contentHorseTakesRider(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void grumpyWolfIgnoresSit(GameTestHelper helper) {
        CareGameTests.grumpyWolfIgnoresSit(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void wolfStandsUpWhenGrumpy(GameTestHelper helper) {
        CareGameTests.wolfStandsUpWhenGrumpy(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void grumpyWolfFollows(GameTestHelper helper) {
        CareGameTests.grumpyWolfFollows(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void grumpyWolfSulksWhenOwnerHurt(GameTestHelper helper) {
        CareGameTests.grumpyWolfSulksWhenOwnerHurt(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void grumpyWolfFightsBack(GameTestHelper helper) {
        CareGameTests.grumpyWolfFightsBack(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void fedGrumpyWolfObeysAgain(GameTestHelper helper) {
        CareGameTests.fedGrumpyWolfObeysAgain(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void grumpyPetShowsInTheBook(GameTestHelper helper) {
        CareGameTests.grumpyPetShowsInTheBook(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hurtWolfStopsSulking(GameTestHelper helper) {
        CareGameTests.hurtWolfStopsSulking(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void tamedParrotIsNeverGrumpy(GameTestHelper helper) {
        CareGameTests.tamedParrotIsNeverGrumpy(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void frozenMemberIsNotChargedTwice(GameTestHelper helper) {
        CareGameTests.frozenMemberIsNotChargedTwice(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void tamedWolfLastsAWeekUnfed(GameTestHelper helper) {
        CareGameTests.tamedWolfLastsAWeekUnfed(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void sulkingWolfDoesNotFollow(GameTestHelper helper) {
        CareGameTests.sulkingWolfDoesNotFollow(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void farOwnerGetsNoSulk(GameTestHelper helper) {
        CareGameTests.farOwnerGetsNoSulk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void fightingWolfDoesNotSulk(GameTestHelper helper) {
        CareGameTests.fightingWolfDoesNotSulk(helper);
    }
}
