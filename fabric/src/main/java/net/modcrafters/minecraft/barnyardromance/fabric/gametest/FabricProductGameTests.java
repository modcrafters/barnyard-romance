package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.ProductGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricProductGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void oldTraitsLoadWithoutLactation(GameTestHelper helper) {
        ProductGameTests.oldTraitsLoadWithoutLactation(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void cakeTakesEveryMilk(GameTestHelper helper) {
        ProductGameTests.cakeTakesEveryMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void milkBucketsAreInTheCommonTag(GameTestHelper helper) {
        ProductGameTests.milkBucketsAreInTheCommonTag(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void goatMilkClearsEffects(GameTestHelper helper) {
        ProductGameTests.goatMilkClearsEffects(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void freshMotherCowGivesMilk(GameTestHelper helper) {
        ProductGameTests.freshMotherCowGivesMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void goatGivesGoatMilk(GameTestHelper helper) {
        ProductGameTests.goatGivesGoatMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void sheepGivesSheepMilk(GameTestHelper helper) {
        ProductGameTests.sheepGivesSheepMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void refusedMilkingKeepsTheBucket(GameTestHelper helper) {
        ProductGameTests.refusedMilkingKeepsTheBucket(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void largeCowMilksTwiceADay(GameTestHelper helper) {
        ProductGameTests.largeCowMilksTwiceADay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 200)
    public void birthStartsMilk(GameTestHelper helper) {
        ProductGameTests.birthStartsMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void wildCowGetsOneMilkWindow(GameTestHelper helper) {
        ProductGameTests.wildCowGetsOneMilkWindow(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void mooshroomBowlStaysVanilla(GameTestHelper helper) {
        ProductGameTests.mooshroomBowlStaysVanilla(helper);
    }
}
