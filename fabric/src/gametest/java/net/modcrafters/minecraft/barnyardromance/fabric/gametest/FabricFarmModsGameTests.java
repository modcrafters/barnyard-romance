package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.FarmModsGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricFarmModsGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void expectedFarmModsAreLoaded(GameTestHelper helper) {
        FarmModsGameTests.expectedFarmModsAreLoaded(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void farmModLaddersLoad(GameTestHelper helper) {
        FarmModsGameTests.farmModLaddersLoad(helper);
    }
}
