package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

// Filling and draining a pail for anything outside it: other mods' tanks and pipes through each loader's adapter, and
// the pail's own screen. Amounts are millilitres. Filling checks the barnyard_romance:pail_fluids tag and draining
// doesn't, so a pail holding a fluid that has since left the tag can still be emptied.
public final class PailTransfer {
    private PailTransfer() {
    }

    // How much of the fluid the pail took, up to amount. Only a single pail fills: a stack of them would share one amount.
    public static int insert(ItemStack pail, Fluid fluid, int amount, boolean simulate) {
        if (pail.getCount() != 1 || !(pail.getItem() instanceof PailItem item) || !PailFluids.accepts(fluid)) {
            return 0;
        }
        return apply(pail, PailItem.contents(pail).fill(BuiltInRegistries.FLUID.getKey(fluid), amount, item.capacity()), simulate);
    }

    // How much the pail gave up, up to amount. A null fluid takes whatever it holds, even a fluid that isn't registered
    // any more; any other gives only itself.
    public static int extract(ItemStack pail, @Nullable Fluid fluid, int amount, boolean simulate) {
        if (pail.getCount() != 1 || !(pail.getItem() instanceof PailItem)) {
            return 0;
        }
        PailContents contents = PailItem.contents(pail);
        if (fluid != null && !contents.holds(BuiltInRegistries.FLUID.getKey(fluid))) {
            return 0;
        }
        return apply(pail, contents.drain(amount), simulate);
    }

    // The fluid in the pail, if it's still registered: what an adapter offers to take out.
    public static Optional<Fluid> fluid(ItemStack pail) {
        Identifier id = PailItem.contents(pail).fluid();
        return id == null ? Optional.empty() : PailFluids.registered(id);
    }

    private static int apply(ItemStack pail, PailContents.Moved moved, boolean simulate) {
        if (!simulate && moved.amount() > 0) {
            PailItem.setContents(pail, moved.contents());
        }
        return moved.amount();
    }
}
