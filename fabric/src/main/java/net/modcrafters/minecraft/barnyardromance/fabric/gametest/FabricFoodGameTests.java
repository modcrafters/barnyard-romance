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

    @GameTest(structure = PEN, maxTicks = 20)
    public void grazersRefuseMeat(GameTestHelper helper) {
        FoodGameTests.grazersRefuseMeat(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void dietFoodFeeds(GameTestHelper helper) {
        FoodGameTests.dietFoodFeeds(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void carnivoresGetLittleFromSalad(GameTestHelper helper) {
        FoodGameTests.carnivoresGetLittleFromSalad(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hurtAnimalsHealFromFoodAnyTime(GameTestHelper helper) {
        FoodGameTests.hurtAnimalsHealFromFoodAnyTime(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void horsesHealLikeVanilla(GameTestHelper helper) {
        FoodGameTests.horsesHealLikeVanilla(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void oldPregnancyLoadsWithoutCare(GameTestHelper helper) {
        FoodGameTests.oldPregnancyLoadsWithoutCare(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void pregnancyCareSavesAndSyncs(GameTestHelper helper) {
        FoodGameTests.pregnancyCareSavesAndSyncs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void breedersMashSpeedsAPregnancy(GameTestHelper helper) {
        FoodGameTests.breedersMashSpeedsAPregnancy(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void goldenBranFeedsTheLitter(GameTestHelper helper) {
        FoodGameTests.goldenBranFeedsTheLitter(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void superfoodsNeedAPregnantMother(GameTestHelper helper) {
        FoodGameTests.superfoodsNeedAPregnantMother(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void superfoodRecipesLoad(GameTestHelper helper) {
        FoodGameTests.superfoodRecipesLoad(helper);
    }
}
