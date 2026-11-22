package net.modcrafters.minecraft.barnyardromance.fabric.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.modcrafters.minecraft.barnyardromance.gametest.GameTestCompat;
import net.modcrafters.minecraft.barnyardromance.pail.MilkFluids;
import net.modcrafters.minecraft.barnyardromance.pail.PailContents;
import net.modcrafters.minecraft.barnyardromance.pail.PailItem;
import net.modcrafters.minecraft.barnyardromance.pail.PailItems;

import java.util.List;

import static net.modcrafters.minecraft.barnyardromance.fabric.gametest.FabricBreedingGameTests.PEN;

// Pails and milk buckets as other mods' tanks and pipes see them, through Fabric's Transfer API. Fabric counts fluid in
// droplets, 81 to the millilitre.
public class FabricPailFluidTests {
    private static final long DROPLETS = 81;

    // How much moved, in droplets, and the item it left behind: a pail with its new contents, or the bucket a bucket became.
    private record Result(long amount, ItemStack stack) {
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aTankFillsAnEmptyPail(GameTestHelper helper) {
        Result result = fill(helper, new ItemStack(PailItems.WOODEN_PAIL), MilkFluids.GOAT.value(), 1000 * DROPLETS, false);
        helper.assertTrue(result.amount() == 1000 * DROPLETS, "an empty pail took " + result.amount());
        helper.assertTrue(PailItem.contents(result.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 1000)),
                "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aPailRefusesASecondMilk(GameTestHelper helper) {
        Result result = fill(helper, pail(MilkFluids.GOAT_ID, 1000), MilkFluids.SHEEP.value(), 1000 * DROPLETS, false);
        helper.assertTrue(result.amount() == 0, "a pail of goat milk took " + result.amount() + " of sheep milk");
        helper.assertTrue(PailItem.contents(result.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 1000)),
                "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aPailRefusesWater(GameTestHelper helper) {
        Result result = fill(helper, new ItemStack(PailItems.IRON_PAIL), Fluids.WATER, 1000 * DROPLETS, false);
        helper.assertTrue(result.amount() == 0, "a pail took " + result.amount() + " of water");
        helper.assertTrue(PailItem.contents(result.stack()).isEmpty(), "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void aPailFillsToCapacityOnly(GameTestHelper helper) {
        Result result = fill(helper, pail(MilkFluids.GOAT_ID, 1500), MilkFluids.GOAT.value(), 1000 * DROPLETS, false);
        helper.assertTrue(result.amount() == 500 * DROPLETS, "a wooden pail holding 1500 took " + result.amount());
        helper.assertTrue(PailItem.contents(result.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 2000)),
                "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void simulateChangesNothing(GameTestHelper helper) {
        Result filled = fill(helper, new ItemStack(PailItems.WOODEN_PAIL), MilkFluids.GOAT.value(), 1000 * DROPLETS, true);
        helper.assertTrue(filled.amount() == 1000 * DROPLETS, "a simulated fill would take " + filled.amount());
        helper.assertTrue(PailItem.contents(filled.stack()).isEmpty(), "a simulated fill left " + PailItem.contents(filled.stack()));
        Result drained = drain(helper, pail(MilkFluids.GOAT_ID, 1000), MilkFluids.GOAT.value(), 500 * DROPLETS, true);
        helper.assertTrue(drained.amount() == 500 * DROPLETS, "a simulated drain would give " + drained.amount());
        helper.assertTrue(PailItem.contents(drained.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 1000)),
                "a simulated drain left " + PailItem.contents(drained.stack()));
        helper.succeed();
    }

    // Water stands in for a fluid a datapack took out of barnyard_romance:pail_fluids: the pail still gives it up.
    @GameTest(structure = PEN, maxTicks = 20)
    public void aDroppedFluidStillDrains(GameTestHelper helper) {
        Identifier water = BuiltInRegistries.FLUID.getKey(Fluids.WATER);
        Result result = drain(helper, pail(water, 1000), Fluids.WATER, 500 * DROPLETS, false);
        helper.assertTrue(result.amount() == 500 * DROPLETS, "a pail of water gave " + result.amount());
        helper.assertTrue(PailItem.contents(result.stack()).equals(new PailContents(water, 500)),
                "the pail holds " + PailItem.contents(result.stack()));
        helper.succeed();
    }

    // Fabric API knows none of these: vanilla's milk bucket isn't a BucketItem, and the other two are the mod's.
    @GameTest(structure = PEN, maxTicks = 20)
    public void milkBucketsEmptyIntoTanks(GameTestHelper helper) {
        for (Holder<Fluid> milk : List.of(MilkFluids.COW, MilkFluids.GOAT, MilkFluids.SHEEP)) {
            Result result = drain(helper, new ItemStack(milk.value().getBucket()), milk.value(), 1000 * DROPLETS, false);
            helper.assertTrue(result.amount() == 1000 * DROPLETS, "a bucket of " + milk.getRegisteredName() + " gave " + result.amount());
            helper.assertTrue(result.stack().is(Items.BUCKET), "a drained bucket of " + milk.getRegisteredName() + " became " + result.stack());
        }
        helper.succeed();
    }

    @GameTest(structure = PEN, maxTicks = 20)
    public void emptyBucketsFillWithMilk(GameTestHelper helper) {
        for (Holder<Fluid> milk : List.of(MilkFluids.COW, MilkFluids.GOAT, MilkFluids.SHEEP)) {
            Result result = fill(helper, new ItemStack(Items.BUCKET), milk.value(), 1000 * DROPLETS, false);
            helper.assertTrue(result.amount() == 1000 * DROPLETS, "an empty bucket took " + result.amount() + " of " + milk.getRegisteredName());
            helper.assertTrue(result.stack().is(milk.value().getBucket()), "a bucket filled with " + milk.getRegisteredName() + " became " + result.stack());
        }
        helper.succeed();
    }

    // A pail keeps whole millilitres, so the 40 droplets left over are refused rather than rounded into milk.
    @GameTest(structure = PEN, maxTicks = 20)
    public void oddDropletsMoveWholeMillilitres(GameTestHelper helper) {
        Result filled = fill(helper, new ItemStack(PailItems.WOODEN_PAIL), MilkFluids.GOAT.value(), 1000 * DROPLETS + 40, false);
        helper.assertTrue(filled.amount() == 1000 * DROPLETS, "a pail took " + filled.amount() + " of 81040 droplets");
        helper.assertTrue(PailItem.contents(filled.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 1000)),
                "the pail holds " + PailItem.contents(filled.stack()));
        Result drained = drain(helper, pail(MilkFluids.GOAT_ID, 1000), MilkFluids.GOAT.value(), 40, false);
        helper.assertTrue(drained.amount() == 0, "a pail gave " + drained.amount() + " droplets when asked for 40");
        helper.assertTrue(PailItem.contents(drained.stack()).equals(new PailContents(MilkFluids.GOAT_ID, 1000)),
                "the pail holds " + PailItem.contents(drained.stack()));
        helper.succeed();
    }

    private static ItemStack pail(Identifier fluid, int amount) {
        ItemStack pail = new ItemStack(PailItems.WOODEN_PAIL);
        PailItem.setContents(pail, new PailContents(fluid, amount));
        return pail;
    }

    private static Result fill(GameTestHelper helper, ItemStack stack, Fluid fluid, long droplets, boolean simulate) {
        return move(helper, stack, true, fluid, droplets, simulate);
    }

    private static Result drain(GameTestHelper helper, ItemStack stack, Fluid fluid, long droplets, boolean simulate) {
        return move(helper, stack, false, fluid, droplets, simulate);
    }

    // Through the item's storage in a container slot, as a tank's pipe would, so a bucket can turn into an empty one.
    // A simulation is a transaction that's never committed.
    private static Result move(GameTestHelper helper, ItemStack stack, boolean fill, Fluid fluid, long droplets, boolean simulate) {
        SimpleContainer slot = new SimpleContainer(stack);
        Storage<FluidVariant> storage = ContainerItemContext.ofSingleSlot(ContainerStorage.of(slot, null).getSlot(0)).find(FluidStorage.ITEM);
        if (storage == null) {
            throw GameTestCompat.failure(helper, stack + " has no fluid storage");
        }
        long moved;
        try (Transaction transaction = Transaction.openOuter()) {
            moved = fill
                    ? storage.insert(FluidVariant.of(fluid), droplets, transaction)
                    : storage.extract(FluidVariant.of(fluid), droplets, transaction);
            if (!simulate) {
                transaction.commit();
            }
        }
        return new Result(moved, slot.getItem(0));
    }
}
