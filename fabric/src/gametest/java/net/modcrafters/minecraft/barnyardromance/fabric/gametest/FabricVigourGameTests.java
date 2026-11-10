package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.VigourGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricVigourGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void vigourRefillsAtMidnightAfterAFedDay(GameTestHelper helper) {
        VigourGameTests.vigourRefillsAtMidnightAfterAFedDay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void vigourCarriesOverAfterAHungryDay(GameTestHelper helper) {
        VigourGameTests.vigourCarriesOverAfterAHungryDay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aMealAfterAMissedDayFillsVigour(GameTestHelper helper) {
        VigourGameTests.aMealAfterAMissedDayFillsVigour(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void hungryPaymentCostsHealthNotLife(GameTestHelper helper) {
        VigourGameTests.hungryPaymentCostsHealthNotLife(helper);
    }
}
