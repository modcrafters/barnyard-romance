package net.modcrafters.minecraft.barnyardromance.core.data;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.UnaryOperator;
//? if <1.20.5 {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
*///?}

// What a stack carries under one of the mod's own component types. Minecraft 1.20.1 has no item components (a stack
// keeps its data in an NBT tag), so shared code goes through here and each version keeps the data its own way. On
// 1.20.1 a value sits in the stack's tag under its type's id (barnyard_romance:<path>), saved with the type's codec;
// no key means no value, and a tag left empty is dropped, so a stack without the mod's data stacks with a plain one.
public final class ItemData {
    private ItemData() {
    }

    public static <T> @Nullable T get(ItemStack stack, DataComponentType<T> type) {
        //? if >=1.20.5 {
        return stack.get(type);
        //?} else {
        /*CompoundTag tag = stack.getTag();
        String key = key(type);
        if (tag == null || !tag.contains(key)) {
            return null;
        }
        return type.codec().parse(NbtOps.INSTANCE, tag.get(key))
                .resultOrPartial(error -> Constants.LOG.warn("Ignoring {} on {}: {}", key, stack, error))
                .orElse(null);
        *///?}
    }

    public static <T> T getOrDefault(ItemStack stack, DataComponentType<T> type, T fallback) {
        //? if >=1.20.5 {
        return stack.getOrDefault(type, fallback);
        //?} else {
        /*T value = get(stack, type);
        return value != null ? value : fallback;
        *///?}
    }

    public static <T> void set(ItemStack stack, DataComponentType<T> type, @Nullable T value) {
        //? if >=1.20.5 {
        stack.set(type, value);
        //?} else {
        /*if (value == null) {
            remove(stack, type);
            return;
        }
        Tag encoded = type.codec().encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, error -> {
        });
        stack.getOrCreateTag().put(key(type), encoded);
        *///?}
    }

    public static boolean has(ItemStack stack, DataComponentType<?> type) {
        //? if >=1.20.5 {
        return stack.has(type);
        //?} else {
        /*CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key(type));
        *///?}
    }

    public static void remove(ItemStack stack, DataComponentType<?> type) {
        //? if >=1.20.5 {
        stack.remove(type);
        //?} else {
        /*CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(key(type));
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
        *///?}
    }

    public static <T> T update(ItemStack stack, DataComponentType<T> type, T fallback, UnaryOperator<T> change) {
        //? if >=1.20.5 {
        return stack.update(type, fallback, change);
        //?} else {
        /*T previous = get(stack, type);
        set(stack, type, change.apply(previous != null ? previous : fallback));
        return previous;
        *///?}
    }

    //? if <1.20.5 {
    /*private static String key(DataComponentType<?> type) {
        return type.id().toString();
    }
    *///?}
}
