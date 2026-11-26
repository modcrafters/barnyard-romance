package net.modcrafters.minecraft.barnyardromance.neoforge.gametest;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.modcrafters.minecraft.barnyardromance.gametest.BreedingGameTests;
import net.modcrafters.minecraft.barnyardromance.gametest.GameTestCompat;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.List;
//? if >=1.21.9 {
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
//?} else {
/*import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
*///?}

// Pails and milk buckets as other mods' tanks and pipes see them, through NeoForge's fluid capability. Amounts are
// millibuckets, which are the pail's millilitres.
public final class NeoForgePailFluidTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("a_tank_fills_an_empty_pail", 20, NeoForgePailFluidTests::aTankFillsAnEmptyPail),
            new BreedingGameTests.Case("a_pail_refuses_a_second_milk", 20, NeoForgePailFluidTests::aPailRefusesASecondMilk),
            new BreedingGameTests.Case("a_pail_refuses_water", 20, NeoForgePailFluidTests::aPailRefusesWater),
            new BreedingGameTests.Case("a_pail_fills_to_capacity_only", 20, NeoForgePailFluidTests::aPailFillsToCapacityOnly),
            new BreedingGameTests.Case("simulate_changes_nothing", 20, NeoForgePailFluidTests::simulateChangesNothing),
            new BreedingGameTests.Case("a_dropped_fluid_still_drains", 20, NeoForgePailFluidTests::aDroppedFluidStillDrains),
            new BreedingGameTests.Case("milk_buckets_empty_into_tanks", 20, NeoForgePailFluidTests::milkBucketsEmptyIntoTanks),
            new BreedingGameTests.Case("empty_buckets_fill_with_milk", 20, NeoForgePailFluidTests::emptyBucketsFillWithMilk)
    );

    // How much moved, and the item it left behind: a pail with its new contents, or the bucket a bucket became.
    private record Result(int amount, ItemStack stack) {
    }

    private NeoForgePailFluidTests() {
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

    // Vanilla's milk bucket is NeoForge's own; goat and sheep milk buckets are the mod's.
    public static void milkBucketsEmptyIntoTanks(GameTestHelper helper) {
        for (Holder<Fluid> milk : List.of(MilkFluids.COW, MilkFluids.GOAT, MilkFluids.SHEEP)) {
            Result result = drain(helper, new ItemStack(milk.value().getBucket()), milk.value(), 1000, false);
            helper.assertTrue(result.amount() == 1000, "a bucket of " + milk.getRegisteredName() + " gave " + result.amount());
            helper.assertTrue(result.stack().is(Items.BUCKET), "a drained bucket of " + milk.getRegisteredName() + " became " + result.stack());
        }
        helper.succeed();
    }

    // NeoForge's own bucket handler fills a bucket with any fluid that names its bucket, so this needs nothing from the mod.
    public static void emptyBucketsFillWithMilk(GameTestHelper helper) {
        for (Holder<Fluid> milk : List.of(MilkFluids.COW, MilkFluids.GOAT, MilkFluids.SHEEP)) {
            Result result = fill(helper, new ItemStack(Items.BUCKET), milk.value(), 1000, false);
            helper.assertTrue(result.amount() == 1000, "an empty bucket took " + result.amount() + " of " + milk.getRegisteredName());
            helper.assertTrue(result.stack().is(milk.value().getBucket()), "a bucket filled with " + milk.getRegisteredName() + " became " + result.stack());
        }
        helper.succeed();
    }

    private static ItemStack pail(Identifier fluid, int amount) {
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

    // Through the item's capability, as a tank's pipe would. From 1.21.9 the item sits in a container slot, so a bucket
    // can turn into an empty one; before that, the handler hands back its container.
    private static Result move(GameTestHelper helper, ItemStack stack, boolean fill, Fluid fluid, int amount, boolean simulate) {
        //? if >=1.21.9 {
        SimpleContainer slot = new SimpleContainer(stack);
        ResourceHandler<FluidResource> handler = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(slot), 0).getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) {
            throw GameTestCompat.failure(helper, stack + " has no fluid capability");
        }
        int moved;
        try (Transaction transaction = Transaction.openRoot()) {
            moved = fill
                    ? handler.insert(FluidResource.of(fluid), amount, transaction)
                    : handler.extract(FluidResource.of(fluid), amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
        }
        return new Result(moved, slot.getItem(0));
        //?} else {
        /*IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            throw GameTestCompat.failure(helper, stack + " has no fluid capability");
        }
        IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
        int moved = fill
                ? handler.fill(new FluidStack(fluid, amount), action)
                : handler.drain(new FluidStack(fluid, amount), action).getAmount();
        return new Result(moved, handler.getContainer());
        *///?}
    }
}
