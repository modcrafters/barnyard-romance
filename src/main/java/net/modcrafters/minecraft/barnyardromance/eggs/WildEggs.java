package net.modcrafters.minecraft.barnyardromance.eggs;

import net.modcrafters.minecraft.barnyardromance.config.BarnyardConfig;
import net.modcrafters.minecraft.barnyardromance.config.Range;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardComponents;
import net.modcrafters.minecraft.barnyardromance.item.BarnyardItems;
import net.modcrafters.minecraft.barnyardromance.pregnancy.LitterRoll;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalLong;

// A fertilized egg that leaves a nesting box goes wild: it forgets its parents and its countdown, keeps its species
// and variant, and stacks with the other wild eggs of its kind. Put back in a box, it starts a fresh countdown, as if
// from an average hen. The stack is changed in place, so it doesn't matter which way it left the box.
public final class WildEggs {
    public static final int STACK = 16;
    // The weight that picks a wild egg's hatch time in its species' range: neither heavy nor light.
    private static final double AVERAGE_WEIGHT = 0.5;

    private WildEggs() {
    }

    public static boolean wild(ItemStack stack) {
        return stack.has(BarnyardComponents.WILD_EGG);
    }

    // Turns a fertilized egg wild, or takes a wild egg's countdown away. Anything else is left alone.
    public static ItemStack taint(ItemStack stack) {
        if (!stack.is(BarnyardItems.FERTILIZED_EGG)) {
            return stack;
        }
        EggData data = stack.get(BarnyardComponents.EGG_DATA);
        WildEgg wild = stack.get(BarnyardComponents.WILD_EGG);
        WildEgg outside;
        if (data != null) {
            outside = WildEgg.of(data.species(), data.variant());
        } else if (wild != null) {
            outside = wild.outside();
        } else {
            return stack;
        }
        stack.remove(BarnyardComponents.EGG_DATA);
        stack.set(BarnyardComponents.WILD_EGG, outside);
        stack.set(DataComponents.MAX_STACK_SIZE, STACK);
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.barnyard_romance.fertilized_egg.wild"));
        return stack;
    }

    // A wild egg that has just gone into a box gets its countdown; true when it changed.
    public static boolean start(ItemStack stack, RandomSource random) {
        WildEgg wild = stack.get(BarnyardComponents.WILD_EGG);
        if (wild == null || wild.started()) {
            return false;
        }
        Range range = BarnyardConfig.current().eggsFor(wild.species()).hatchTicks();
        stack.set(BarnyardComponents.WILD_EGG, wild.withTicks(Math.max(1L, LitterRoll.pickTicks(range, AVERAGE_WEIGHT, random.nextDouble()))));
        return true;
    }

    // How long an egg in a box has left, for its tooltip and the box window; empty for an egg with no countdown.
    public static OptionalLong remainingTicks(ItemStack stack) {
        EggData data = stack.get(BarnyardComponents.EGG_DATA);
        if (data != null) {
            return OptionalLong.of(data.remainingTicks());
        }
        WildEgg wild = stack.get(BarnyardComponents.WILD_EGG);
        return wild != null && wild.started() ? OptionalLong.of(wild.remainingTicks()) : OptionalLong.empty();
    }
}
