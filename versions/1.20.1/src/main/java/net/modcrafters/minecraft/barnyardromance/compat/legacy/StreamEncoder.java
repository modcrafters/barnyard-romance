package net.modcrafters.minecraft.barnyardromance.compat.legacy;

// 1.20.5's StreamEncoder, for Minecraft 1.20.1, which doesn't have it.
@FunctionalInterface
public interface StreamEncoder<O, T> {
    void encode(O buf, T value);
}
