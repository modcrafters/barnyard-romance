package net.modcrafters.minecraft.barnyardromance.compat.legacy;

// 1.20.5's StreamDecoder, for Minecraft 1.20.1, which doesn't have it. Only the 1.20.1 build has this package.
@FunctionalInterface
public interface StreamDecoder<I, T> {
    T decode(I buf);
}
