package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

// Fabric runs these through the fabric-gametest entrypoint; the bodies are shared in common.
public class FabricBreedingGameTests {
    private static final String PEN = "barnyard_romance:gametest/pen";

    @GameTest(structure = PEN, maxTicks = 200)
    public void pigsConceiveALitterAndRest(GameTestHelper helper) {
        BreedingGameTests.pigsConceiveALitterAndRest(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void cowsCarryOneCalfForAboutThreeDays(GameTestHelper helper) {
        BreedingGameTests.cowsCarryOneCalfForAboutThreeDays(helper);
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
}
