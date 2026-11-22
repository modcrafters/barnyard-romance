package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.PailGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricPailGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void pailsHaveTheirCapacities(GameTestHelper helper) {
        PailGameTests.pailsHaveTheirCapacities(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aNewPailIsEmpty(GameTestHelper helper) {
        PailGameTests.aNewPailIsEmpty(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void pailsAreInThePailsTag(GameTestHelper helper) {
        PailGameTests.pailsAreInThePailsTag(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void pailRecipesCraft(GameTestHelper helper) {
        PailGameTests.pailRecipesCraft(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void theBowlAndBucketStillCraft(GameTestHelper helper) {
        PailGameTests.theBowlAndBucketStillCraft(helper);
    }
}
