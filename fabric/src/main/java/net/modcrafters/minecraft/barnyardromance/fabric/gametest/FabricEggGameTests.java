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
    public void boxTakesTenEggs(GameTestHelper helper) {
        EggGameTests.boxTakesTenEggs(helper);
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

    @GameTest(structure = PEN, maxTicks = 80)
    public void countdownPausesOutsideTheBox(GameTestHelper helper) {
        EggGameTests.countdownPausesOutsideTheBox(helper);
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
    public void blockedFrontHatchesOnTop(GameTestHelper helper) {
        EggGameTests.blockedFrontHatchesOnTop(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hatchingCatchesUpAfterUnload(GameTestHelper helper) {
        EggGameTests.hatchingCatchesUpAfterUnload(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void henLaysIntoANearbyBox(GameTestHelper helper) {
        EggGameTests.henLaysIntoANearbyBox(helper);
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

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungryRoosterFertilizesNothing(GameTestHelper helper) {
        EggGameTests.hungryRoosterFertilizesNothing(helper);
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
}
