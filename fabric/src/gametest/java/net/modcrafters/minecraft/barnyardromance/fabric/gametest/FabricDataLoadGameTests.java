package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.DataLoadGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricDataLoadGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void modDataLoads(GameTestHelper helper) {
        DataLoadGameTests.modDataLoads(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void vanillaFoodsInCTags(GameTestHelper helper) {
        DataLoadGameTests.vanillaFoodsInCTags(helper);
    }
}
