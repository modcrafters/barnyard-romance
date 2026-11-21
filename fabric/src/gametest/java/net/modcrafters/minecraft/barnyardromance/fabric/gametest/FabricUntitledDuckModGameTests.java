package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.UntitledDuckModGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricUntitledDuckModGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void duckLaysByOurRulesAndItsTimerIsHeld(GameTestHelper helper) {
        UntitledDuckModGameTests.duckLaysByOurRulesAndItsTimerIsHeld(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedDuckKeepsItsOwnTimer(GameTestHelper helper) {
        UntitledDuckModGameTests.excludedDuckKeepsItsOwnTimer(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void foodDoesNotBreedBirds(GameTestHelper helper) {
        UntitledDuckModGameTests.foodDoesNotBreedBirds(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aThrownDuckEggNeverHatches(GameTestHelper helper) {
        UntitledDuckModGameTests.aThrownDuckEggNeverHatches(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anExcludedBirdsThrownEggsHatch(GameTestHelper helper) {
        UntitledDuckModGameTests.anExcludedBirdsThrownEggsHatch(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void tamingFoodIsRefusedAndFamiliarityTames(GameTestHelper helper) {
        UntitledDuckModGameTests.tamingFoodIsRefusedAndFamiliarityTames(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedDuckIsTamedByTheModsFood(GameTestHelper helper) {
        UntitledDuckModGameTests.excludedDuckIsTamedByTheModsFood(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aSackedDuckKeepsItsTraits(GameTestHelper helper) {
        UntitledDuckModGameTests.aSackedDuckKeepsItsTraits(helper);
    }
}
