package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.modcrafters.minecraft.barnyardromance.gametest.ApiGameTests;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricApiGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void anEmptyMilkingResultIsRefused(GameTestHelper helper) {
        ApiGameTests.anEmptyMilkingResultIsRefused(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anEmptyDeathDropIsRefused(GameTestHelper helper) {
        ApiGameTests.anEmptyDeathDropIsRefused(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anEmptyShearYieldIsRefused(GameTestHelper helper) {
        ApiGameTests.anEmptyShearYieldIsRefused(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void infoAgreesWithTheStatCard(GameTestHelper helper) {
        ApiGameTests.infoAgreesWithTheStatCard(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void untrackedAnimalsHaveNoInfo(GameTestHelper helper) {
        ApiGameTests.untrackedAnimalsHaveNoInfo(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void cancelledMilkingKeepsTheBucket(GameTestHelper helper) {
        ApiGameTests.cancelledMilkingKeepsTheBucket(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void milkingResultCanBeChanged(GameTestHelper helper) {
        ApiGameTests.milkingResultCanBeChanged(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void deathDropCanBeReplaced(GameTestHelper helper) {
        ApiGameTests.deathDropCanBeReplaced(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aHugeDeathDropSplitsIntoStacks(GameTestHelper helper) {
        ApiGameTests.aHugeDeathDropSplitsIntoStacks(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void shearYieldCanBeCancelled(GameTestHelper helper) {
        ApiGameTests.shearYieldCanBeCancelled(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aThrowingListenerDoesNotStopMilking(GameTestHelper helper) {
        ApiGameTests.aThrowingListenerDoesNotStopMilking(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void disabledSpeciesFireNoMilking(GameTestHelper helper) {
        ApiGameTests.disabledSpeciesFireNoMilking(helper);
    }
}
