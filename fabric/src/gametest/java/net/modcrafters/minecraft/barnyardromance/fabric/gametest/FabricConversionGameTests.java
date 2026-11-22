package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.ConversionGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

// Fabric runs these through the fabric-gametest entrypoint; the bodies are shared in common.
public class FabricConversionGameTests {
    static final String PEN = "barnyard_romance:gametest/pen";

    @GameTest(structure = PEN, maxTicks = 20)
    public void aShearedMooshroomBecomesACowWithItsRecord(GameTestHelper helper) {
        ConversionGameTests.aShearedMooshroomBecomesACowWithItsRecord(helper);
    }
}
