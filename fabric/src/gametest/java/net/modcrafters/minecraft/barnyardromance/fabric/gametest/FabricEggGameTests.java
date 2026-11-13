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
    public void otherLayersWalkToABox(GameTestHelper helper) {
        EggGameTests.otherLayersWalkToABox(helper);
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
    public void boxTakesSixteenEggs(GameTestHelper helper) {
        EggGameTests.boxTakesSixteenEggs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void plainEggsStackInOneSlot(GameTestHelper helper) {
        EggGameTests.plainEggsStackInOneSlot(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void plainEggsCannotBePutIn(GameTestHelper helper) {
        EggGameTests.plainEggsCannotBePutIn(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hopperPullsOnlyPlainEggs(GameTestHelper helper) {
        EggGameTests.hopperPullsOnlyPlainEggs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void boxDropsEggsWhenBroken(GameTestHelper helper) {
        EggGameTests.boxDropsEggsWhenBroken(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void boxRecipe(GameTestHelper helper) {
        EggGameTests.boxRecipe(helper);
    }

    @GameTest(structure = PEN, maxTicks = 40)
    public void eggHatchesInTheBox(GameTestHelper helper) {
        EggGameTests.eggHatchesInTheBox(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hatchedChickInherits(GameTestHelper helper) {
        EggGameTests.hatchedChickInherits(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hatchedChickJoinsMothersBook(GameTestHelper helper) {
        EggGameTests.hatchedChickJoinsMothersBook(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anEggLaidInAnOldRangeHatchesInTheNew(GameTestHelper helper) {
        EggGameTests.anEggLaidInAnOldRangeHatchesInTheNew(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void blockedFrontHatchesOnTop(GameTestHelper helper) {
        EggGameTests.blockedFrontHatchesOnTop(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hatchingCatchesUpAfterUnload(GameTestHelper helper) {
        EggGameTests.hatchingCatchesUpAfterUnload(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void timeSkipCountsEggsDown(GameTestHelper helper) {
        EggGameTests.timeSkipCountsEggsDown(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void lateHatchIsBornWhenDue(GameTestHelper helper) {
        EggGameTests.lateHatchIsBornWhenDue(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void failedHatchKeepsTheEgg(GameTestHelper helper) {
        EggGameTests.failedHatchKeepsTheEgg(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void skippedEggsAreNotLaid(GameTestHelper helper) {
        EggGameTests.skippedEggsAreNotLaid(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void oldLayingCountsEveryEggAsLaid(GameTestHelper helper) {
        EggGameTests.oldLayingCountsEveryEggAsLaid(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void henLaysIntoANearbyBox(GameTestHelper helper) {
        EggGameTests.henLaysIntoANearbyBox(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void henUsesABoxWithAFenceOnTop(GameTestHelper helper) {
        EggGameTests.henUsesABoxWithAFenceOnTop(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void boxEggIsFertilizedWithAFedRooster(GameTestHelper helper) {
        EggGameTests.boxEggIsFertilizedWithAFedRooster(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void heavyHenEggHatchesSooner(GameTestHelper helper) {
        EggGameTests.heavyHenEggHatchesSooner(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void closestRoosterIsTheFather(GameTestHelper helper) {
        EggGameTests.closestRoosterIsTheFather(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void eachFertilizedEggCostsTheRooster15(GameTestHelper helper) {
        EggGameTests.eachFertilizedEggCostsTheRooster15(helper);
    }

    @GameTest(structure = PEN, maxTicks = 40)
    public void aSpentRoosterLaysPlain(GameTestHelper helper) {
        EggGameTests.aSpentRoosterLaysPlain(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aRoosterRunsOutMidSitting(GameTestHelper helper) {
        EggGameTests.aRoosterRunsOutMidSitting(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aRoosterInAnotherPenDoesNotFertilize(GameTestHelper helper) {
        EggGameTests.aRoosterInAnotherPenDoesNotFertilize(helper);
    }

    @GameTest(structure = PEN, maxTicks = 40)
    public void groundEggsCostTheRoosterNothing(GameTestHelper helper) {
        EggGameTests.groundEggsCostTheRoosterNothing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aHungryRoosterPaysHealthPerEgg(GameTestHelper helper) {
        EggGameTests.aHungryRoosterPaysHealthPerEgg(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void groundEggsArePlainWithARooster(GameTestHelper helper) {
        EggGameTests.groundEggsArePlainWithARooster(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void farBoxIsIgnored(GameTestHelper helper) {
        EggGameTests.farBoxIsIgnored(helper);
    }

    @GameTest(structure = PEN, maxTicks = 40)
    public void secondHenDoesNotShareABox(GameTestHelper helper) {
        EggGameTests.secondHenDoesNotShareABox(helper);
    }

    @GameTest(structure = PEN, maxTicks = 200)
    public void unreachableBoxGivesUp(GameTestHelper helper) {
        EggGameTests.unreachableBoxGivesUp(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void sittingHenReservesHerSlot(GameTestHelper helper) {
        EggGameTests.sittingHenReservesHerSlot(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void henInBoxStaysInHerdBook(GameTestHelper helper) {
        EggGameTests.henInBoxStaysInHerdBook(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void breakingBoxReleasesHenAndEggs(GameTestHelper helper) {
        EggGameTests.breakingBoxReleasesHenAndEggs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void boxSurvivesSaveWithHen(GameTestHelper helper) {
        EggGameTests.boxSurvivesSaveWithHen(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void henSitsLongerForMoreEggs(GameTestHelper helper) {
        EggGameTests.henSitsLongerForMoreEggs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void boxTakesEveryEggOfASitting(GameTestHelper helper) {
        EggGameTests.boxTakesEveryEggOfASitting(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void eggsThatDoNotFitComeOutWithHer(GameTestHelper helper) {
        EggGameTests.eggsThatDoNotFitComeOutWithHer(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void dueEggsAreLaidTogether(GameTestHelper helper) {
        EggGameTests.dueEggsAreLaidTogether(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void eggDueWhileSittingIsLaidAfter(GameTestHelper helper) {
        EggGameTests.eggDueWhileSittingIsLaidAfter(helper);
    }

    @GameTest(structure = PEN, maxTicks = 40)
    public void walkingHenKeepsHerNextEgg(GameTestHelper helper) {
        EggGameTests.walkingHenKeepsHerNextEgg(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void leashedHenLaysOnTheGround(GameTestHelper helper) {
        EggGameTests.leashedHenLaysOnTheGround(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void henComesOutDespiteACopy(GameTestHelper helper) {
        EggGameTests.henComesOutDespiteACopy(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void herdBookShowsFertilizedEggs(GameTestHelper helper) {
        EggGameTests.herdBookShowsFertilizedEggs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void herdBookSaysWhyEggsArePlain(GameTestHelper helper) {
        EggGameTests.herdBookSaysWhyEggsArePlain(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void herdBookShowsNoEggsForAnUnfedHen(GameTestHelper helper) {
        EggGameTests.herdBookShowsNoEggsForAnUnfedHen(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void herdBookShowsNoEggsForRoosters(GameTestHelper helper) {
        EggGameTests.herdBookShowsNoEggsForRoosters(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void warmHenLaysBrownEggs(GameTestHelper helper) {
        EggGameTests.warmHenLaysBrownEggs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 80)
    public void aShiftClickedEggGoesWild(GameTestHelper helper) {
        EggGameTests.aShiftClickedEggGoesWild(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anEggTakenByHandGoesWild(GameTestHelper helper) {
        EggGameTests.anEggTakenByHandGoesWild(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anEggSwappedOutGoesWild(GameTestHelper helper) {
        EggGameTests.anEggSwappedOutGoesWild(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void wildEggsOfAKindStack(GameTestHelper helper) {
        EggGameTests.wildEggsOfAKindStack(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWildEggPutBackStartsAfresh(GameTestHelper helper) {
        EggGameTests.aWildEggPutBackStartsAfresh(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void wildEggsGoInOneToANest(GameTestHelper helper) {
        EggGameTests.wildEggsGoInOneToANest(helper);
    }

    @GameTest(structure = PEN, maxTicks = 40)
    public void aWildEggHatchesAChickWithNoParents(GameTestHelper helper) {
        EggGameTests.aWildEggHatchesAChickWithNoParents(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aThrownWildEggHatchesTainted(GameTestHelper helper) {
        EggGameTests.aThrownWildEggHatchesTainted(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void onlyABoxEggWarnsAboutTakingItOut(GameTestHelper helper) {
        EggGameTests.onlyABoxEggWarnsAboutTakingItOut(helper);
    }
}
