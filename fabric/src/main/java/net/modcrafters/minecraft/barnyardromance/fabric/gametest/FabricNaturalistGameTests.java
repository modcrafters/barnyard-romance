package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.NaturalistGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricNaturalistGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void livestockIsTracked(GameTestHelper helper) {
        NaturalistGameTests.livestockIsTracked(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void othersAreIgnored(GameTestHelper helper) {
        NaturalistGameTests.othersAreIgnored(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void deerAndBoarGetPregnant(GameTestHelper helper) {
        NaturalistGameTests.deerAndBoarGetPregnant(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void ostrichesNeedAMaleAndAFemale(GameTestHelper helper) {
        NaturalistGameTests.ostrichesNeedAMaleAndAFemale(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void ostrichesBreedByCourtship(GameTestHelper helper) {
        NaturalistGameTests.ostrichesBreedByCourtship(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void foodDoesNotBreedDucksOrTurkeys(GameTestHelper helper) {
        NaturalistGameTests.foodDoesNotBreedDucksOrTurkeys(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void duckTimerIsHeldOff(GameTestHelper helper) {
        NaturalistGameTests.duckTimerIsHeldOff(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedDuckKeepsNaturalistsTimer(GameTestHelper helper) {
        NaturalistGameTests.excludedDuckKeepsNaturalistsTimer(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void duckWithNoBoxLaysPlainEggsOnTheGround(GameTestHelper helper) {
        NaturalistGameTests.duckWithNoBoxLaysPlainEggsOnTheGround(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void duckUsesABoxAndEggsAreFertilized(GameTestHelper helper) {
        NaturalistGameTests.duckUsesABoxAndEggsAreFertilized(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void turkeyLaysVanillaEggsWithNoMale(GameTestHelper helper) {
        NaturalistGameTests.turkeyLaysVanillaEggsWithNoMale(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void turkeyHatchesTurkeys(GameTestHelper helper) {
        NaturalistGameTests.turkeyHatchesTurkeys(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void roosterDoesNotFertilizeOtherBirds(GameTestHelper helper) {
        NaturalistGameTests.roosterDoesNotFertilizeOtherBirds(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void familiarityTamesDucksAndOstriches(GameTestHelper helper) {
        NaturalistGameTests.familiarityTamesDucksAndOstriches(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void duckOnWaterUsesABox(GameTestHelper helper) {
        NaturalistGameTests.duckOnWaterUsesABox(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void sittingDuckLaysWhereSheSits(GameTestHelper helper) {
        NaturalistGameTests.sittingDuckLaysWhereSheSits(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void familiarLivestockStaysNearPlayers(GameTestHelper helper) {
        NaturalistGameTests.familiarLivestockStaysNearPlayers(helper);
    }
}
