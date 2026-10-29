package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.ProductGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricProductGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void sheepShearOnceADay(GameTestHelper helper) {
        ProductGameTests.sheepShearOnceADay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void unfamiliarSheepKeepTheirWool(GameTestHelper helper) {
        ProductGameTests.unfamiliarSheepKeepTheirWool(helper);
    }

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

    @GameTest(structure = PEN, maxTicks = 20)
    public void sizedDropsFollowTheRule(GameTestHelper helper) {
        ProductGameTests.sizedDropsFollowTheRule(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void meatKeepsItsCurve(GameTestHelper helper) {
        ProductGameTests.meatKeepsItsCurve(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void bigSheepShearMoreWool(GameTestHelper helper) {
        ProductGameTests.bigSheepShearMoreWool(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void wolfTamedByFamiliarity(GameTestHelper helper) {
        ProductGameTests.wolfTamedByFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void wolvesBiteAndLastByTheirSize(GameTestHelper helper) {
        ProductGameTests.wolvesBiteAndLastByTheirSize(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void bigTamedWolvesKeepTheirHealth(GameTestHelper helper) {
        ProductGameTests.bigTamedWolvesKeepTheirHealth(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void catTamedByFamiliarity(GameTestHelper helper) {
        ProductGameTests.catTamedByFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void ocelotTrustsByFamiliarity(GameTestHelper helper) {
        ProductGameTests.ocelotTrustsByFamiliarity(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void bonesNoLongerTameWolves(GameTestHelper helper) {
        ProductGameTests.bonesNoLongerTameWolves(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void felinesEatMeatForLess(GameTestHelper helper) {
        ProductGameTests.felinesEatMeatForLess(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void befriendedFelinesStayAround(GameTestHelper helper) {
        ProductGameTests.befriendedFelinesStayAround(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void unfamiliarAnimalsKeepTheirProducts(GameTestHelper helper) {
        ProductGameTests.unfamiliarAnimalsKeepTheirProducts(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void oldTraitsLoadWithoutHunger(GameTestHelper helper) {
        ProductGameTests.oldTraitsLoadWithoutHunger(helper);
    }
}
