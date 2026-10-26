package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.FoodGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricFoodGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void builtInDietsLoad(GameTestHelper helper) {
        FoodGameTests.builtInDietsLoad(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void groupTagHoldsVanillaMeat(GameTestHelper helper) {
        FoodGameTests.groupTagHoldsVanillaMeat(helper);
    }
}
