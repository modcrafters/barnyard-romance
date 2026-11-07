package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.FeederGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

// The hopper tests run real hoppers, so on Fabric they also check the block works with hoppers here.
public class FabricFeederGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void feederTakesWheatButNotBeef(GameTestHelper helper) {
        FeederGameTests.feederTakesWheatButNotBeef(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void feederRefusesSuperfoods(GameTestHelper helper) {
        FeederGameTests.feederRefusesSuperfoods(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void birdFeederTakesSeeds(GameTestHelper helper) {
        FeederGameTests.birdFeederTakesSeeds(helper);
    }

    @GameTest(structure = PEN, maxTicks = 140)
    public void aHopperFillsAMangerAndKeepsWhatItRefuses(GameTestHelper helper) {
        FeederGameTests.aHopperFillsAMangerAndKeepsWhatItRefuses(helper);
    }

    @GameTest(structure = PEN, maxTicks = 140)
    public void aHopperCannotEmptyAManger(GameTestHelper helper) {
        FeederGameTests.aHopperCannotEmptyAManger(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void breakingAMangerDropsItsFood(GameTestHelper helper) {
        FeederGameTests.breakingAMangerDropsItsFood(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aMealComesFromTheFirstFullSlot(GameTestHelper helper) {
        FeederGameTests.aMealComesFromTheFirstFullSlot(helper);
    }
}
