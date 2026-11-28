package net.modcrafters.minecraft.barnyardromance.forge.gametest;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.GameTestCompat;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;

import java.util.List;

// Pails and milk buckets as other mods' tanks and pipes see them, through Forge's fluid capability, as
// NeoForgePailFluidTests checks them on NeoForge 1.21.1. Amounts are millibuckets, which are the pail's millilitres.
public final class ForgePailFluidTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_tank_fills_an_empty_pail", 20, ForgePailFluidTests::aTankFillsAnEmptyPail),
            new BreedingGameTests.Case("a_pail_refuses_a_second_milk", 20, ForgePailFluidTests::aPailRefusesASecondMilk),
            new BreedingGameTests.Case("a_pail_refuses_water", 20, ForgePailFluidTests::aPailRefusesWater),
            new BreedingGameTests.Case("a_pail_fills_to_capacity_only", 20, ForgePailFluidTests::aPailFillsToCapacityOnly),
            new BreedingGameTests.Case("simulate_changes_nothing", 20, ForgePailFluidTests::simulateChangesNothing),
            new BreedingGameTests.Case("a_dropped_fluid_still_drains", 20, ForgePailFluidTests::aDroppedFluidStillDrains),
            new BreedingGameTests.Case("milk_buckets_empty_into_tanks", 20, ForgePailFluidTests::milkBucketsEmptyIntoTanks),
            new BreedingGameTests.Case("empty_buckets_fill_with_milk", 20, ForgePailFluidTests::emptyBucketsFillWithMilk)
    );

    // How much moved, and the item it left behind: a pail with its new contents, or the bucket a bucket became.
    private record Result(int amount, ItemStack stack) {
    }

    private ForgePailFluidTests() {
    }

    public static void aTankFillsAnEmptyPail(GameTestHelper helper) {
        Result result = fill(helper, new ItemStack(PailItems.WOODEN_PAIL), MilkFluids.GOAT.value(), 1000, false);
        helper.assertTrue(result.amount() == 1000, "an empty pail took " + result.amount());
        helper.assertTrue(PailItem.contents(result.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 1000)),
                "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    public static void aPailRefusesASecondMilk(GameTestHelper helper) {
        Result result = fill(helper, pail(MilkFluids.GOAT_ID, 1000), MilkFluids.SHEEP.value(), 1000, false);
        helper.assertTrue(result.amount() == 0, "a pail of goat milk took " + result.amount() + " of sheep milk");
        helper.assertTrue(PailItem.contents(result.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 1000)),
                "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    public static void aPailRefusesWater(GameTestHelper helper) {
        Result result = fill(helper, new ItemStack(PailItems.IRON_PAIL), Fluids.WATER, 1000, false);
        helper.assertTrue(result.amount() == 0, "a pail took " + result.amount() + " of water");
        helper.assertTrue(PailItem.contents(result.stack()).isEmpty(), "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    public static void aPailFillsToCapacityOnly(GameTestHelper helper) {
        Result result = fill(helper, pail(MilkFluids.GOAT_ID, 1500), MilkFluids.GOAT.value(), 1000, false);
        helper.assertTrue(result.amount() == 500, "a wooden pail holding 1500 took " + result.amount());
        helper.assertTrue(PailItem.contents(result.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 2000)),
                "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    public static void simulateChangesNothing(GameTestHelper helper) {
        Result filled = fill(helper, new ItemStack(PailItems.WOODEN_PAIL), MilkFluids.GOAT.value(), 1000, true);
        helper.assertTrue(filled.amount() == 1000, "a simulated fill would take " + filled.amount());
        helper.assertTrue(PailItem.contents(filled.stack()).isEmpty(), "a simulated fill left " + PailItem.contents(filled.stack()));
        Result drained = drain(helper, pail(MilkFluids.GOAT_ID, 1000), MilkFluids.GOAT.value(), 500, true);
        helper.assertTrue(drained.amount() == 500, "a simulated drain would give " + drained.amount());
        helper.assertTrue(PailItem.contents(drained.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 1000)),
                "a simulated drain left " + PailItem.contents(drained.stack()));
        helper.succeed();
    }

    // Water stands in for a fluid a datapack took out of barnyard_romance:pail_fluids: the pail still gives it up.
    public static void aDroppedFluidStillDrains(GameTestHelper helper) {
        Result result = drain(helper, pail(BuiltInRegistries.FLUID.getKey(Fluids.WATER), 1000), Fluids.WATER, 500, false);
        helper.assertTrue(result.amount() == 500, "a pail of water gave " + result.amount());
        helper.assertTrue(PailItem.contents(result.stack()).equals(new PailContents(BuiltInRegistries.FLUID.getKey(Fluids.WATER), 500)),
                "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    // Vanilla's milk bucket is Forge's own milk; goat and sheep milk buckets are the mod's.
    public static void milkBucketsEmptyIntoTanks(GameTestHelper helper) {
        for (Holder<Fluid> milk : List.of(MilkFluids.COW, MilkFluids.GOAT, MilkFluids.SHEEP)) {
            Result result = drain(helper, new ItemStack(milk.value().getBucket()), milk.value(), 1000, false);
            helper.assertTrue(result.amount() == 1000, "a bucket of " + name(milk) + " gave " + result.amount());
            helper.assertTrue(result.stack().is(Items.BUCKET), "a drained bucket of " + name(milk) + " became " + result.stack());
        }
        helper.succeed();
    }

    // Forge's own bucket handler fills a bucket with any fluid that names its bucket, so this needs nothing from the mod.
    public static void emptyBucketsFillWithMilk(GameTestHelper helper) {
        for (Holder<Fluid> milk : List.of(MilkFluids.COW, MilkFluids.GOAT, MilkFluids.SHEEP)) {
            Result result = fill(helper, new ItemStack(Items.BUCKET), milk.value(), 1000, false);
            helper.assertTrue(result.amount() == 1000, "an empty bucket took " + result.amount() + " of " + name(milk));
            helper.assertTrue(result.stack().is(milk.value().getBucket()), "a bucket filled with " + name(milk) + " became " + result.stack());
        }
        helper.succeed();
    }

    private static ResourceLocation name(Holder<Fluid> fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid.value());
    }

    private static ItemStack pail(ResourceLocation fluid, int amount) {
        ItemStack pail = new ItemStack(PailItems.WOODEN_PAIL);
        PailItem.setContents(pail, new PailContents(fluid, amount));
        return pail;
    }

    private static Result fill(GameTestHelper helper, ItemStack stack, Fluid fluid, int amount, boolean simulate) {
        return move(helper, stack, true, fluid, amount, simulate);
    }

    private static Result drain(GameTestHelper helper, ItemStack stack, Fluid fluid, int amount, boolean simulate) {
        return move(helper, stack, false, fluid, amount, simulate);
    }

    // Through the item's capability, as a tank's pipe would; the handler hands back its container.
    private static Result move(GameTestHelper helper, ItemStack stack, boolean fill, Fluid fluid, int amount, boolean simulate) {
        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve().orElse(null);
        if (handler == null) {
            throw GameTestCompat.failure(helper, stack + " has no fluid capability");
        }
        IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        int moved = fill
                ? handler.fill(new FluidStack(fluid, amount), action)
                : handler.drain(new FluidStack(fluid, amount), action).getAmount();
        return new Result(moved, handler.getContainer());
    }
}
