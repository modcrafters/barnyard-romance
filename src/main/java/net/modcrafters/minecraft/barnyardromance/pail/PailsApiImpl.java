package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.modcrafters.minecraft.barnyardromance.api.BarnyardPails;

import java.util.Optional;

// The public API's view of pails: PailTransfer's rules, so the API, the screen and the adapters can't disagree.
final class PailsApiImpl implements BarnyardPails {
    @Override
    public boolean isPail(ItemStack stack) {
        return stack.getItem() instanceof PailItem;
    }

    @Override
    public int capacity(ItemStack stack) {
        return stack.getItem() instanceof PailItem pail ? pail.capacity() : 0;
    }

    @Override
    public Optional<Fluid> fluid(ItemStack stack) {
        return isPail(stack) ? PailTransfer.fluid(stack) : Optional.empty();
    }

    @Override
    public int amount(ItemStack stack) {
        return isPail(stack) ? PailItem.contents(stack).amount() : 0;
    }

    @Override
    public int fill(ItemStack pail, Fluid fluid, int millilitres, boolean simulate) {
        return PailTransfer.insert(pail, fluid, millilitres, simulate);
    }

    @Override
    public int drain(ItemStack pail, int millilitres, boolean simulate) {
        return PailTransfer.extract(pail, null, millilitres, simulate);
    }
}
