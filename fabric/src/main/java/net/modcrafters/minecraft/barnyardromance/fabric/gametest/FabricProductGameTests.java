package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.ProductGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricProductGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void oldTraitsLoadWithoutLactation(GameTestHelper helper) {
        ProductGameTests.oldTraitsLoadWithoutLactation(helper);
    }
}
