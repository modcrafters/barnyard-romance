package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.Optional;

// Milking into a pail. The species' milk list still names a milk bucket; the pail takes that bucket's fluid instead,
// one bucket's worth at a time, and only all of it.
public final class PailMilking {
    public static final int MILKING = 1000;

    private PailMilking() {
    }

    public static boolean fits(ItemStack pail, Item result) {
        return PailFluids.forBucket(result, PailItem.contents(pail).fluid()).filter(fluid -> PailTransfer.insert(pail, fluid, MILKING, true) == MILKING).isPresent();
    }

    // Another milk comes before a full pail, since emptying some of it wouldn't help.
    public static Optional<Component> refusal(ItemStack pail, Item result) {
        Optional<Fluid> fluid = PailFluids.forBucket(result, PailItem.contents(pail).fluid());
        if (!(pail.getItem() instanceof PailItem item) || fluid.isEmpty()) {
            return Optional.empty();
        }
        PailContents contents = PailItem.contents(pail);
        if (!contents.isEmpty() && !contents.holds(BuiltInRegistries.FLUID.getKey(fluid.get()))) {
            return Optional.of(Component.translatable("message.barnyard_romance.pail.holds", PailFluids.name(contents.fluid())));
        }
        if (contents.space(item.capacity()) < MILKING) {
            return Optional.of(Component.translatable("message.barnyard_romance.pail.full"));
        }
        return Optional.empty();
    }

    public static boolean fill(ItemStack pail, Item result) {
        if (!fits(pail, result)) {
            return false;
        }
        return PailTransfer.insert(pail, PailFluids.forBucket(result, PailItem.contents(pail).fluid()).orElseThrow(), MILKING, false) == MILKING;
    }
}
