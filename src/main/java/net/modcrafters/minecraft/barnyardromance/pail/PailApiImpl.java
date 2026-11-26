package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.pail.api.PailApi;

import java.util.Optional;

final class PailApiImpl implements PailApi {
    @Override
    public boolean isPail(ItemStack stack) {
        return stack.getItem() instanceof PailItem;
    }

    @Override
    public boolean fits(ItemStack pail, Item result) {
        return PailMilking.fits(pail, result);
    }

    @Override
    public Optional<Component> refusal(ItemStack pail, Item result) {
        return PailMilking.refusal(pail, result);
    }

    @Override
    public boolean fill(ItemStack pail, Item result) {
        return PailMilking.fill(pail, result);
    }
}
