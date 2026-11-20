package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.AutumnityEnvironmentalGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricAutumnityEnvironmentalGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void turkeyLaysByOurRulesAndItsTimerIsHeld(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.turkeyLaysByOurRulesAndItsTimerIsHeld(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedTurkeyKeepsItsOwnTimer(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.excludedTurkeyKeepsItsOwnTimer(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void foodDoesNotBreedATurkey(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.foodDoesNotBreedATurkey(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aThrownTurkeyEggNeverHatches(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.aThrownTurkeyEggNeverHatches(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anExcludedTurkeysThrownEggsHatch(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.anExcludedTurkeysThrownEggsHatch(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void duckLaysByOurRulesAndItsTimerIsHeld(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.duckLaysByOurRulesAndItsTimerIsHeld(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedDuckKeepsItsOwnTimer(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.excludedDuckKeepsItsOwnTimer(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void foodDoesNotBreedADuck(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.foodDoesNotBreedADuck(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aThrownDuckEggNeverHatches(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.aThrownDuckEggNeverHatches(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anExcludedDucksThrownEggsHatch(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.anExcludedDucksThrownEggsHatch(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aYakIsMilkedByOurRules(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.aYakIsMilkedByOurRules(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aYakIsShearedOnceADay(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.aYakIsShearedOnceADay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aDispenserShearsAYakOnceADay(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.aDispenserShearsAYakOnceADay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 200)
    public void aTrackedSowsLitterIsTheBarnyards(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.aTrackedSowsLitterIsTheBarnyards(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anExcludedSowKeepsEnvironmentalsLargerLitter(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.anExcludedSowKeepsEnvironmentalsLargerLitter(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aSowWithPregnancyOffKeepsEnvironmentalsLargerLitter(GameTestHelper helper) {
        AutumnityEnvironmentalGameTests.aSowWithPregnancyOffKeepsEnvironmentalsLargerLitter(helper);
    }
}
