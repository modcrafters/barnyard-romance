package net.modcrafters.minecraft.barnyardromance.compat.legacy;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

// One of the mod's item components on Minecraft 1.20.1, which has none (they came in 1.20.5): a key with the codec
// that saves its value. ItemData keeps the value in the stack's tag under the id.
public record DataComponentType<T>(ResourceLocation id, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
}
