package net.modcrafters.minecraft.barnyardromance.compat.environmental;

import net.modcrafters.minecraft.barnyardromance.pregnancy.Litter;

// Environmental adds extra piglets and hoglets to every breeding in a BabyEntitySpawnEvent handler. The barnyard rolls
// the litters it conceives itself, so the extras only go to breedings it leaves to vanilla: a species a pack switches
// off, or any breeding while pregnancy is switched off.
public final class EnvironmentalLitters {
    private EnvironmentalLitters() {
    }

    // Whether the barnyard sets the litter for this breeding event: the event was posted while it conceived a litter
    // (see Litter.conceive). Reading the flag, not the parents, means every breeding that ends in a vanilla birth keeps
    // Environmental's extras, whatever the reason the barnyard stepped aside.
    public static boolean ownsLitter(Object event) {
        return Litter.conceiving();
    }
}
