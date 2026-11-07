package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.CourtshipGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricCourtshipGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void aFedMaleDoesNotFallInLove(GameTestHelper helper) {
        CourtshipGameTests.aFedMaleDoesNotFallInLove(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aFemaleInLoveIsMatchedWithOneMale(GameTestHelper helper) {
        CourtshipGameTests.aFemaleInLoveIsMatchedWithOneMale(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aFenceKeepsAMaleOut(GameTestHelper helper) {
        CourtshipGameTests.aFenceKeepsAMaleOut(helper);
    }

    @GameTest(structure = PEN, maxTicks = 800)
    public void aMaleOnlyMatesTheFemaleHeWasChosenFor(GameTestHelper helper) {
        CourtshipGameTests.aMaleOnlyMatesTheFemaleHeWasChosenFor(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aHungryMaleAtTheCostIsNotChosen(GameTestHelper helper) {
        CourtshipGameTests.aHungryMaleAtTheCostIsNotChosen(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aHungryMalePaysHealthToMate(GameTestHelper helper) {
        CourtshipGameTests.aHungryMalePaysHealthToMate(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void goatsBreedByCourtship(GameTestHelper helper) {
        CourtshipGameTests.goatsBreedByCourtship(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void horsesBreedByCourtship(GameTestHelper helper) {
        CourtshipGameTests.horsesBreedByCourtship(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void petsTakeTheClosestMale(GameTestHelper helper) {
        CourtshipGameTests.petsTakeTheClosestMale(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void reachIgnoresWhereTheMaleIsWalking(GameTestHelper helper) {
        CourtshipGameTests.reachIgnoresWhereTheMaleIsWalking(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aChosenMaleWalksToAFarFemale(GameTestHelper helper) {
        CourtshipGameTests.aChosenMaleWalksToAFarFemale(helper);
    }
}
