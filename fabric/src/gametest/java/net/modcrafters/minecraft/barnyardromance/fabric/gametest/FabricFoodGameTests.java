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

    @GameTest(structure = PEN, maxTicks = 20)
    public void dairyMashStartsMilk(GameTestHelper helper) {
        FoodGameTests.dairyMashStartsMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void ordinaryFoodCanStartMilk(GameTestHelper helper) {
        FoodGameTests.ordinaryFoodCanStartMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aKnownAnswerFollowsAFoodFileReload(GameTestHelper helper) {
        FoodGameTests.aKnownAnswerFollowsAFoodFileReload(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aKnownAnswerFollowsAConfigChange(GameTestHelper helper) {
        FoodGameTests.aKnownAnswerFollowsAConfigChange(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void dairyMashIsRefusedByAMilkingCow(GameTestHelper helper) {
        FoodGameTests.dairyMashIsRefusedByAMilkingCow(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void dairyMashIsRefusedByABull(GameTestHelper helper) {
        FoodGameTests.dairyMashIsRefusedByABull(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void studMashRestoresHalfTheVigour(GameTestHelper helper) {
        FoodGameTests.studMashRestoresHalfTheVigour(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void studMashDoesNotCountAsAMeal(GameTestHelper helper) {
        FoodGameTests.studMashDoesNotCountAsAMeal(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void studMashSkipsTheDailyRule(GameTestHelper helper) {
        FoodGameTests.studMashSkipsTheDailyRule(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void studMashIsRefusedByFemalesAndYoung(GameTestHelper helper) {
        FoodGameTests.studMashIsRefusedByFemalesAndYoung(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void studMashTopsOutAtFull(GameTestHelper helper) {
        FoodGameTests.studMashTopsOutAtFull(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void dairyMashNeedsFamiliarity(GameTestHelper helper) {
        FoodGameTests.dairyMashNeedsFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void foodScanReadsRealRecipes(GameTestHelper helper) {
        FoodGameTests.foodScanReadsRealRecipes(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void ourCreativeTabHoldsEveryItem(GameTestHelper helper) {
        FoodGameTests.ourCreativeTabHoldsEveryItem(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void everyRecipeUnlocksInTheRecipeBook(GameTestHelper helper) {
        FoodGameTests.everyRecipeUnlocksInTheRecipeBook(helper);
    }
}
