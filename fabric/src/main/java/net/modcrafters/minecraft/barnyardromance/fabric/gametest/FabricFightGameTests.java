package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.FightGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricFightGameTests {
    @GameTest(structure = PEN, maxTicks = 1200)
    public void twoBullsFightOverACowUntilOneDies(GameTestHelper helper) {
        FightGameTests.twoBullsFightOverACowUntilOneDies(helper);
    }

    @GameTest(structure = PEN, maxTicks = 400)
    public void aThirdBullMatesWhileTwoFight(GameTestHelper helper) {
        FightGameTests.aThirdBullMatesWhileTwoFight(helper);
    }

    @GameTest(structure = PEN, maxTicks = 1200)
    public void theBiggerBullWins(GameTestHelper helper) {
        FightGameTests.theBiggerBullWins(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void foodInHandStopsAFightBetweenFamiliarBulls(GameTestHelper helper) {
        FightGameTests.foodInHandStopsAFightBetweenFamiliarBulls(helper);
    }

    @GameTest(structure = PEN, maxTicks = 1200)
    public void foodDoesNotStopAWildFight(GameTestHelper helper) {
        FightGameTests.foodDoesNotStopAWildFight(helper);
    }

    @GameTest(structure = PEN, maxTicks = 1200)
    public void goatsFightToTheEnd(GameTestHelper helper) {
        FightGameTests.goatsFightToTheEnd(helper);
    }

    @GameTest(structure = PEN, maxTicks = 200)
    public void aFightEndsWhenARivalIsRemoved(GameTestHelper helper) {
        FightGameTests.aFightEndsWhenARivalIsRemoved(helper);
    }

    @GameTest(structure = PEN, maxTicks = 1200)
    public void bystandersAreNotHurt(GameTestHelper helper) {
        FightGameTests.bystandersAreNotHurt(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void petsNeverFight(GameTestHelper helper) {
        FightGameTests.petsNeverFight(helper);
    }

    @GameTest(structure = PEN, maxTicks = 1200)
    public void aRivalDeathIsNotedInTheHerdBook(GameTestHelper helper) {
        FightGameTests.aRivalDeathIsNotedInTheHerdBook(helper);
    }

    @GameTest(structure = PEN, maxTicks = 1200)
    public void roostersFightWithoutAHen(GameTestHelper helper) {
        FightGameTests.roostersFightWithoutAHen(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void familiarBullsNeverFightAtRandom(GameTestHelper helper) {
        FightGameTests.familiarBullsNeverFightAtRandom(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aWildAndAFamiliarBullNeverFightAtRandom(GameTestHelper helper) {
        FightGameTests.aWildAndAFamiliarBullNeverFightAtRandom(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void twoWildBullsFightAtRandom(GameTestHelper helper) {
        FightGameTests.twoWildBullsFightAtRandom(helper);
    }

    @GameTest(structure = PEN, maxTicks = 100)
    public void aMaleRollsOnceADay(GameTestHelper helper) {
        FightGameTests.aMaleRollsOnceADay(helper);
    }
}
