package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

// Fabric runs these through the fabric-gametest entrypoint; the bodies are shared in common.
public class FabricBreedingGameTests {
    static final String PEN = "barnyard_romance:gametest/pen";

    @GameTest(structure = PEN, maxTicks = 200)
    public void pigsConceiveALitterAndRest(GameTestHelper helper) {
        BreedingGameTests.pigsConceiveALitterAndRest(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void cowsCarryOneCalfForAboutThreeDays(GameTestHelper helper) {
        BreedingGameTests.cowsCarryOneCalfForAboutThreeDays(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void timeSkipMovesPregnanciesOn(GameTestHelper helper) {
        BreedingGameTests.timeSkipMovesPregnanciesOn(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void sameSexPairsMakeNothing(GameTestHelper helper) {
        BreedingGameTests.sameSexPairsMakeNothing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void unfamiliarAnimalsDoNotFallInLove(GameTestHelper helper) {
        BreedingGameTests.unfamiliarAnimalsDoNotFallInLove(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hurtingAnAnimalEndsItsLove(GameTestHelper helper) {
        BreedingGameTests.hurtingAnAnimalEndsItsLove(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void pigsInLoveBreedOnTheirOwn(GameTestHelper helper) {
        BreedingGameTests.pigsInLoveBreedOnTheirOwn(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void wornOutMothersRestWithoutConceiving(GameTestHelper helper) {
        BreedingGameTests.wornOutMothersRestWithoutConceiving(helper);
    }

    @GameTest(structure = PEN, maxTicks = 200)
    public void aBirthCostsTheMotherFertility(GameTestHelper helper) {
        BreedingGameTests.aBirthCostsTheMotherFertility(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void youngAnimalsDoNotFallInLove(GameTestHelper helper) {
        BreedingGameTests.youngAnimalsDoNotFallInLove(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void oldAnimalsDieOfOldAge(GameTestHelper helper) {
        BreedingGameTests.oldAnimalsDieOfOldAge(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void animalsOfUnknownAgeNeverDie(GameTestHelper helper) {
        BreedingGameTests.animalsOfUnknownAgeNeverDie(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void traitsSurviveSaveAndLoad(GameTestHelper helper) {
        BreedingGameTests.traitsSurviveSaveAndLoad(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void calvesRememberBothParents(GameTestHelper helper) {
        BreedingGameTests.calvesRememberBothParents(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedSpeciesKeepTheirTraits(GameTestHelper helper) {
        BreedingGameTests.excludedSpeciesKeepTheirTraits(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedSpeciesCannotJoinABook(GameTestHelper helper) {
        BreedingGameTests.excludedSpeciesCannotJoinABook(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void excludedMembersAreNotRefreshed(GameTestHelper helper) {
        BreedingGameTests.excludedMembersAreNotRefreshed(helper);
    }
}
