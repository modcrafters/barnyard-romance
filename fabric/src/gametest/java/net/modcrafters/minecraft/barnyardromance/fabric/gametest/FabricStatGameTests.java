package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.StatGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricStatGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void aFedGrownBullShowsHisVigour(GameTestHelper helper) {
        StatGameTests.aFedGrownBullShowsHisVigour(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aHungryBullNotesTheHealthCost(GameTestHelper helper) {
        StatGameTests.aHungryBullNotesTheHealthCost(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWildCaughtCowNotesHerCap(GameTestHelper helper) {
        StatGameTests.aWildCaughtCowNotesHerCap(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aLayingHenCountsTodaysEggs(GameTestHelper helper) {
        StatGameTests.aLayingHenCountsTodaysEggs(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aSheepSaysWhetherItCanBeShorn(GameTestHelper helper) {
        StatGameTests.aSheepSaysWhetherItCanBeShorn(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anAnimalWithoutTraitsHasNoFacts(GameTestHelper helper) {
        StatGameTests.anAnimalWithoutTraitsHasNoFacts(helper);
    }
}
