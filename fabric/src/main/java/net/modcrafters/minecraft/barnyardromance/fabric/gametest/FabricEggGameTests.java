package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.EggGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricEggGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void henLaysHerDailyEggsOnTheGround(GameTestHelper helper) {
        EggGameTests.henLaysHerDailyEggsOnTheGround(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void largeHappyHenPlansThree(GameTestHelper helper) {
        EggGameTests.largeHappyHenPlansThree(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungryHenPlansNone(GameTestHelper helper) {
        EggGameTests.hungryHenPlansNone(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void roosterAndChickLayNothing(GameTestHelper helper) {
        EggGameTests.roosterAndChickLayNothing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void vanillaEggTimerIsOff(GameTestHelper helper) {
        EggGameTests.vanillaEggTimerIsOff(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void seedsNeverBreedChickens(GameTestHelper helper) {
        EggGameTests.seedsNeverBreedChickens(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void oldChickenPregnancyIsCleared(GameTestHelper helper) {
        EggGameTests.oldChickenPregnancyIsCleared(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedChickensLayLikeVanilla(GameTestHelper helper) {
        EggGameTests.excludedChickensLayLikeVanilla(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void fertilizedEggsDoNotStack(GameTestHelper helper) {
        EggGameTests.fertilizedEggsDoNotStack(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void fertilizedEggIsAnEgg(GameTestHelper helper) {
        EggGameTests.fertilizedEggIsAnEgg(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void thrownPlainEggNeverHatches(GameTestHelper helper) {
        EggGameTests.thrownPlainEggNeverHatches(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void thrownFertilizedEggHatchesTainted(GameTestHelper helper) {
        EggGameTests.thrownFertilizedEggHatchesTainted(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void thrownFertilizedEggUsesTheChance(GameTestHelper helper) {
        EggGameTests.thrownFertilizedEggUsesTheChance(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedChickensThrownEggsHatch(GameTestHelper helper) {
        EggGameTests.excludedChickensThrownEggsHatch(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void warmHenLaysBrownEggs(GameTestHelper helper) {
        EggGameTests.warmHenLaysBrownEggs(helper);
    }
}
