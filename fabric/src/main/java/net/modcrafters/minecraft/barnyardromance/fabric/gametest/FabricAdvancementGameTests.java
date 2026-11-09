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
}
