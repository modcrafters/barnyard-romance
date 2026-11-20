package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.TrackingGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricTrackingGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void aFoxAndAPandaAreUntracked(GameTestHelper helper) {
        TrackingGameTests.aFoxAndAPandaAreUntracked(helper);
    }

    @GameTest(structure = PEN, maxTicks = 220)
    public void aPregnantFoxLeftUntrackedIsHarmless(GameTestHelper helper) {
        TrackingGameTests.aPregnantFoxLeftUntrackedIsHarmless(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void untrackedFoxesBreedAsVanilla(GameTestHelper helper) {
        TrackingGameTests.untrackedFoxesBreedAsVanilla(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anUntrackedFoxShowsNoStats(GameTestHelper helper) {
        TrackingGameTests.anUntrackedFoxShowsNoStats(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anUntrackedFoxGoesBackToItsVanillaSize(GameTestHelper helper) {
        TrackingGameTests.anUntrackedFoxGoesBackToItsVanillaSize(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void theApiDoesNotReportAnUntrackedFoxPregnant(GameTestHelper helper) {
        TrackingGameTests.theApiDoesNotReportAnUntrackedFoxPregnant(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void theApiDoesNotReportAnUntrackedHensLaying(GameTestHelper helper) {
        TrackingGameTests.theApiDoesNotReportAnUntrackedHensLaying(helper);
    }
}
