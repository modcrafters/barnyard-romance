package net.modcrafters.minecraft.barnyardromance.core.data;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.UnaryOperator;

// What a stack carries under one of the mod's own component types. Minecraft 1.20.1 has no item components (a stack
// keeps its data in an NBT tag), so shared code goes through here and each version keeps the data its own way.
public final class ItemData {
    private ItemData() {
    }

    public static <T> @Nullable T get(ItemStack stack, DataComponentType<T> type) {
        //? if >=1.20.5 {
        return stack.get(type);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static <T> T getOrDefault(ItemStack stack, DataComponentType<T> type, T fallback) {
        //? if >=1.20.5 {
        return stack.getOrDefault(type, fallback);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static <T> void set(ItemStack stack, DataComponentType<T> type, @Nullable T value) {
        //? if >=1.20.5 {
        stack.set(type, value);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static boolean has(ItemStack stack, DataComponentType<?> type) {
        //? if >=1.20.5 {
        return stack.has(type);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static void remove(ItemStack stack, DataComponentType<?> type) {
        //? if >=1.20.5 {
        stack.remove(type);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }

    public static <T> T update(ItemStack stack, DataComponentType<T> type, T fallback, UnaryOperator<T> change) {
        //? if >=1.20.5 {
        return stack.update(type, fallback, change);
        //?} else {
        /*throw new UnsupportedOperationException();
        *///?}
    }
}
