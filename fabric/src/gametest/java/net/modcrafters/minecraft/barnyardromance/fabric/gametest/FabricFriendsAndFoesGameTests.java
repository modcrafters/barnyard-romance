package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.FriendsAndFoesGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricFriendsAndFoesGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void aShearedMoobloomKeepsItsRecord(GameTestHelper helper) {
        FriendsAndFoesGameTests.aShearedMoobloomKeepsItsRecord(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aMoobloomMilksAndGivesStewByOurRules(GameTestHelper helper) {
        FriendsAndFoesGameTests.aMoobloomMilksAndGivesStewByOurRules(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void moobloomStewIsRefusedTodayAfterARefusal(GameTestHelper helper) {
        FriendsAndFoesGameTests.moobloomStewIsRefusedTodayAfterARefusal(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void crabAndPenguinAreOut(GameTestHelper helper) {
        FriendsAndFoesGameTests.crabAndPenguinAreOut(helper);
    }
}
