package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.FeederGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

// The hopper tests run real hoppers, so on Fabric they also check the block works with hoppers here. The herd,
// catch-up and morning feeding tests are the shared ones, wrapped.
public class FabricFeederGameTests {
    @GameTest(structure = PEN, maxTicks = 100)
    public void petBowlFoodTracksDogs(GameTestHelper helper) {
        FeederGameTests.petBowlFoodTracksDogs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void petBowlFoodTracksCats(GameTestHelper helper) {
        FeederGameTests.petBowlFoodTracksCats(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void birdFeederSeedsTrackInventoryAndHerd(GameTestHelper helper) {
        FeederGameTests.birdFeederSeedsTrackInventoryAndHerd(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void troughFoodTracksInventoryAndHerd(GameTestHelper helper) {
        FeederGameTests.troughFoodTracksInventoryAndHerd(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void feederFoodTracksInventoryAndHerd(GameTestHelper helper) {
        FeederGameTests.feederFoodTracksInventoryAndHerd(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void mangerHayTracksInventoryAndHerd(GameTestHelper helper) {
        FeederGameTests.mangerHayTracksInventoryAndHerd(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void feedingBlocksRememberPlacementFacing(GameTestHelper helper) {
        FeederGameTests.feedingBlocksRememberPlacementFacing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void feedingBlockShapesFollowTheirModels(GameTestHelper helper) {
        FeederGameTests.feedingBlockShapesFollowTheirModels(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void feederTakesWheatButNotBeef(GameTestHelper helper) {
        FeederGameTests.feederTakesWheatButNotBeef(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void feederRefusesSuperfoods(GameTestHelper helper) {
        FeederGameTests.feederRefusesSuperfoods(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void birdFeederTakesSeeds(GameTestHelper helper) {
        FeederGameTests.birdFeederTakesSeeds(helper);
    }

    @GameTest(structure = PEN, maxTicks = 140)
    public void aHopperFillsAMangerAndKeepsWhatItRefuses(GameTestHelper helper) {
        FeederGameTests.aHopperFillsAMangerAndKeepsWhatItRefuses(helper);
    }

    @GameTest(structure = PEN, maxTicks = 140)
    public void aHopperCannotEmptyAManger(GameTestHelper helper) {
        FeederGameTests.aHopperCannotEmptyAManger(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void breakingAMangerDropsItsFood(GameTestHelper helper) {
        FeederGameTests.breakingAMangerDropsItsFood(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aMealComesFromTheFirstFullSlot(GameTestHelper helper) {
        FeederGameTests.aMealComesFromTheFirstFullSlot(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aMangerFindsTheCowsInItsPen(GameTestHelper helper) {
        FeederGameTests.aMangerFindsTheCowsInItsPen(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aCowInAPenCornerStaysInTheHerd(GameTestHelper helper) {
        FeederGameTests.aCowInAPenCornerStaysInTheHerd(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void anUnfamiliarCowIsNotInTheHerd(GameTestHelper helper) {
        FeederGameTests.anUnfamiliarCowIsNotInTheHerd(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void catchUpSharesTwoDaysOfFoodOverTenDays(GameTestHelper helper) {
        FeederGameTests.catchUpSharesTwoDaysOfFoodOverTenDays(helper);
    }

    @GameTest(structure = PEN, maxTicks = 140)
    public void aCowWaitsForItsManger(GameTestHelper helper) {
        FeederGameTests.aCowWaitsForItsManger(helper);
    }

    @GameTest(structure = PEN, maxTicks = 240)
    public void breakingTheMangerMidCatchUpLetsTheRestSettleAlone(GameTestHelper helper) {
        FeederGameTests.breakingTheMangerMidCatchUpLetsTheRestSettleAlone(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void catchUpDoesNotWalk(GameTestHelper helper) {
        FeederGameTests.catchUpDoesNotWalk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aCowInTwoHerdsIsSettledOnce(GameTestHelper helper) {
        FeederGameTests.aCowInTwoHerdsIsSettledOnce(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void noneModeCatchUpTakesNoFood(GameTestHelper helper) {
        FeederGameTests.noneModeCatchUpTakesNoFood(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void oneDayModeCatchUpFeedsOneDay(GameTestHelper helper) {
        FeederGameTests.oneDayModeCatchUpFeedsOneDay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void twoMangersCatchingUpAtOnceSettleEachDayOnce(GameTestHelper helper) {
        FeederGameTests.twoMangersCatchingUpAtOnceSettleEachDayOnce(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aDayThePenWasLoadedTakesNoFood(GameTestHelper helper) {
        FeederGameTests.aDayThePenWasLoadedTakesNoFood(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aCowBelowFamiliarGetsNoFood(GameTestHelper helper) {
        FeederGameTests.aCowBelowFamiliarGetsNoFood(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aHerdHenHungryYesterdayLaysNothing(GameTestHelper helper) {
        FeederGameTests.aHerdHenHungryYesterdayLaysNothing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void cowsWalkToTheMangerAndEat(GameTestHelper helper) {
        FeederGameTests.cowsWalkToTheMangerAndEat(helper);
    }

    @GameTest(structure = PEN, maxTicks = 800)
    public void noMoreThanFourWalkAtOnce(GameTestHelper helper) {
        FeederGameTests.noMoreThanFourWalkAtOnce(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aHandFedCowStaysPut(GameTestHelper helper) {
        FeederGameTests.aHandFedCowStaysPut(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aFeederMealIsNotLove(GameTestHelper helper) {
        FeederGameTests.aFeederMealIsNotLove(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void foodAddedAtNoonFeedsTheHungry(GameTestHelper helper) {
        FeederGameTests.foodAddedAtNoonFeedsTheHungry(helper);
    }

    @GameTest(structure = PEN, maxTicks = 300)
    public void aSittingWolfIsNotMoved(GameTestHelper helper) {
        FeederGameTests.aSittingWolfIsNotMoved(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aCatchUpMealHeals(GameTestHelper helper) {
        FeederGameTests.aCatchUpMealHeals(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aCowInACornerWalksOutToEat(GameTestHelper helper) {
        FeederGameTests.aCowInACornerWalksOutToEat(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aMangerMealShowsOnTheFedRow(GameTestHelper helper) {
        FeederGameTests.aMangerMealShowsOnTheFedRow(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aBriefMorningLoadLeavesTheRestOfTheDayToCatchUp(GameTestHelper helper) {
        FeederGameTests.aBriefMorningLoadLeavesTheRestOfTheDayToCatchUp(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void feedingBlockRecipesCraftTheirBlocks(GameTestHelper helper) {
        FeederGameTests.feedingBlockRecipesCraftTheirBlocks(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aCowStaysToEatThenWalksOff(GameTestHelper helper) {
        FeederGameTests.aCowStaysToEatThenWalksOff(helper);
    }

    @GameTest(structure = PEN, maxTicks = 40)
    public void aComparatorReadsHowFullAMangerIs(GameTestHelper helper) {
        FeederGameTests.aComparatorReadsHowFullAMangerIs(helper);
    }
}
