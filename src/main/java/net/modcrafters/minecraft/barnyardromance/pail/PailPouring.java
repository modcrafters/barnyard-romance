package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.world.item.ItemStack;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;

import java.util.Optional;

// The pail window's pour between the pail in hand and whatever fluid container is put in its slot, through each
// loader's own fluid system, so a bucket, another pail and other mods' items all follow the same rule. A container that
// holds no fluid fills from the pail, as much as it takes; any other container pours into the pail, as much as fits.
// Both sides keep their own rules, through their loader adapters: a bucket moves only a whole 1000, and a pail takes
// only the fluids in barnyard_romance:pail_fluids, one at a time, up to its room.
public interface PailPouring {
    PailPouring INSTANCE = Services.load(PailPouring.class);

    // The pour between one container and the pail, worked out on copies: neither argument changes. Empty when nothing
    // would move.
    Optional<Poured> pour(ItemStack container, ItemStack pail);

    // The container's stack after the pour, which may be another item (an empty bucket becomes a milk bucket), and what
    // the pail holds after it.
    record Poured(ItemStack container, PailContents pail) {
    }
}
