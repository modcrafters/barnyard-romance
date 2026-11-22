package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.modcrafters.minecraft.barnyardromance.gametest.MeadowGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

public class FabricMeadowGameTests {
    @GameTest(structure = PEN, maxTicks = 20)
    public void aBullRefusesTheWoodenBucket(GameTestHelper helper) {
        MeadowGameTests.aBullRefusesTheWoodenBucket(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aDryCowRefusesTheWoodenBucket(GameTestHelper helper) {
        MeadowGameTests.aDryCowRefusesTheWoodenBucket(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aMilkedCowGivesWoodenMilk(GameTestHelper helper) {
        MeadowGameTests.aMilkedCowGivesWoodenMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWaterBuffaloGivesBuffaloMilkOnceADay(GameTestHelper helper) {
        MeadowGameTests.aWaterBuffaloGivesBuffaloMilkOnceADay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWarpedWoolyCowGivesWarpedMilk(GameTestHelper helper) {
        MeadowGameTests.aWarpedWoolyCowGivesWarpedMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void takingTheWoodenBucketOffCowsClosesIt(GameTestHelper helper) {
        MeadowGameTests.takingTheWoodenBucketOffCowsClosesIt(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWoolyCowIsShearedOnceADay(GameTestHelper helper) {
        MeadowGameTests.aWoolyCowIsShearedOnceADay(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aPlainWoolyCowFillsAPailWithCowMilk(GameTestHelper helper) {
        MeadowGameTests.aPlainWoolyCowFillsAPailWithCowMilk(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWarpedWoolyCowGivesAPailNothing(GameTestHelper helper) {
        MeadowGameTests.aWarpedWoolyCowGivesAPailNothing(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aWoolyCowStillTakesTheWoodenBucket(GameTestHelper helper) {
        MeadowGameTests.aWoolyCowStillTakesTheWoodenBucket(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void anIronBucketStillDoesNothingOnAWoolyCow(GameTestHelper helper) {
        MeadowGameTests.anIronBucketStillDoesNothingOnAWoolyCow(helper);
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void waterBuffaloAndYakGiveCowMilk(GameTestHelper helper) {
        MeadowGameTests.waterBuffaloAndYakGiveCowMilk(helper);
    }
}
