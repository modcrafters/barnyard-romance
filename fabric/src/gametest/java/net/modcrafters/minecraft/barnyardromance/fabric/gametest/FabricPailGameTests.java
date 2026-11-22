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

    @GameTest(structure = PEN, maxTicks = 20)
    public void milkBucketsMapToTheirFluids(GameTestHelper helper) {
        PailGameTests.milkBucketsMapToTheirFluids(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void fluidsMapBackToTheirBuckets(GameTestHelper helper) {
        PailGameTests.fluidsMapBackToTheirBuckets(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void pailFluidsAreMilkOnly(GameTestHelper helper) {
        PailGameTests.pailFluidsAreMilkOnly(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void ourMilksAreInCMilk(GameTestHelper helper) {
        PailGameTests.ourMilksAreInCMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void milksHaveNamesAndColours(GameTestHelper helper) {
        PailGameTests.milksHaveNamesAndColours(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anUnknownFluidNamesItselfById(GameTestHelper helper) {
        PailGameTests.anUnknownFluidNamesItselfById(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aPailTakesACowsMilk(GameTestHelper helper) {
        PailGameTests.aPailTakesACowsMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aSecondMilkingAddsToThePail(GameTestHelper helper) {
        PailGameTests.aSecondMilkingAddsToThePail(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aFullPailSpendsNothing(GameTestHelper helper) {
        PailGameTests.aFullPailSpendsNothing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aPailOfGoatMilkRefusesACow(GameTestHelper helper) {
        PailGameTests.aPailOfGoatMilkRefusesACow(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aPailObeysTheMilkingRules(GameTestHelper helper) {
        PailGameTests.aPailObeysTheMilkingRules(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aCancelledMilkingLeavesThePail(GameTestHelper helper) {
        PailGameTests.aCancelledMilkingLeavesThePail(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aListenerResultWithoutAFluidMilksNothing(GameTestHelper helper) {
        PailGameTests.aListenerResultWithoutAFluidMilksNothing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aCreativePlayerFillsThePail(GameTestHelper helper) {
        PailGameTests.aCreativePlayerFillsThePail(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anOffhandPailFills(GameTestHelper helper) {
        PailGameTests.anOffhandPailFills(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aConfigEntryNamingAPailWins(GameTestHelper helper) {
        PailGameTests.aConfigEntryNamingAPailWins(helper);
    }
}
