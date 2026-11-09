package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.AdvancementGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

// Fabric runs these through the fabric-gametest entrypoint; the bodies are shared in common.
public class FabricAdvancementGameTests {
    static final String PEN = "barnyard_romance:gametest/pen";

    @GameTest(structure = PEN, maxTicks = 20)
    public void befriendingAWildAnimalEarnsTheRoot(GameTestHelper helper) {
        AdvancementGameTests.befriendingAWildAnimalEarnsTheRoot(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void feedingAnAlreadyFamiliarAnimalDoesNotEarnTheRoot(GameTestHelper helper) {
        AdvancementGameTests.feedingAnAlreadyFamiliarAnimalDoesNotEarnTheRoot(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void heldGrantsAreGivenAtLogin(GameTestHelper helper) {
        AdvancementGameTests.heldGrantsAreGivenAtLogin(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void grantingToAnOfflinePlayerHoldsIt(GameTestHelper helper) {
        AdvancementGameTests.grantingToAnOfflinePlayerHoldsIt(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void twoByTwoCountsACalfBornFromAPregnancy(GameTestHelper helper) {
        AdvancementGameTests.twoByTwoCountsACalfBornFromAPregnancy(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void matingEarnsABunInTheOven(GameTestHelper helper) {
        AdvancementGameTests.matingEarnsABunInTheOven(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aBirthEarnsSpecialDeliveryForTheBreeder(GameTestHelper helper) {
        AdvancementGameTests.aBirthEarnsSpecialDeliveryForTheBreeder(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void offlineBreederIsCreditedAtLogin(GameTestHelper helper) {
        AdvancementGameTests.offlineBreederIsCreditedAtLogin(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aPregnancyWithoutABreederCreditsNobody(GameTestHelper helper) {
        AdvancementGameTests.aPregnancyWithoutABreederCreditsNobody(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void pregnancyOffStillEarnsBoth(GameTestHelper helper) {
        AdvancementGameTests.pregnancyOffStillEarnsBoth(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aGrandchildEarnsFamilyTree(GameTestHelper helper) {
        AdvancementGameTests.aGrandchildEarnsFamilyTree(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aCalfOfWildParentsIsNoFamilyTree(GameTestHelper helper) {
        AdvancementGameTests.aCalfOfWildParentsIsNoFamilyTree(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void tenPigletsEarnTheFirstPigStep(GameTestHelper helper) {
        AdvancementGameTests.tenPigletsEarnTheFirstPigStep(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aDonkeyFoalCountsForTheHorseLadder(GameTestHelper helper) {
        AdvancementGameTests.aDonkeyFoalCountsForTheHorseLadder(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void naturalistLaddersLoadOnlyWithNaturalist(GameTestHelper helper) {
        AdvancementGameTests.naturalistLaddersLoadOnlyWithNaturalist(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aBredEggHatchingCreditsPlayersNearby(GameTestHelper helper) {
        AdvancementGameTests.aBredEggHatchingCreditsPlayersNearby(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWildEggEarnsWhichCameFirstButNoLadder(GameTestHelper helper) {
        AdvancementGameTests.aWildEggEarnsWhichCameFirstButNoLadder(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aThrownBredEggCounts(GameTestHelper helper) {
        AdvancementGameTests.aThrownBredEggCounts(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void milkingAFamiliarCowEarnsUdderTrust(GameTestHelper helper) {
        AdvancementGameTests.milkingAFamiliarCowEarnsUdderTrust(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void handShearingEarnsFleeceNavidad(GameTestHelper helper) {
        AdvancementGameTests.handShearingEarnsFleeceNavidad(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void dispenserShearingCreditsNobody(GameTestHelper helper) {
        AdvancementGameTests.dispenserShearingCreditsNobody(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void killingAFamiliarCowIsDinner(GameTestHelper helper) {
        AdvancementGameTests.killingAFamiliarCowIsDinner(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void killingATamedPetIsNotDinner(GameTestHelper helper) {
        AdvancementGameTests.killingATamedPetIsNotDinner(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void writingAnAnimalIntoAHerdBookEarnsPaperTrail(GameTestHelper helper) {
        AdvancementGameTests.writingAnAnimalIntoAHerdBookEarnsPaperTrail(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aFightStartingEarnsLockingHorns(GameTestHelper helper) {
        AdvancementGameTests.aFightStartingEarnsLockingHorns(helper);
    }
}
