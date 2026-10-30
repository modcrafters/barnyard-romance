package net.modcrafters.minecraft.barnyardromance.nest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

// A hen sitting in a nesting box: her whole saved self (like a bee in a hive), the egg she'll leave in her nest,
// the game time she comes out and the nest she has.
public record NestOccupant(CompoundTag hen, ItemStack egg, long exitAt, int slot, UUID id) {
    public static final Codec<NestOccupant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompoundTag.CODEC.fieldOf("hen").forGetter(NestOccupant::hen),
            ItemStack.CODEC.fieldOf("egg").forGetter(NestOccupant::egg),
            Codec.LONG.fieldOf("exitAt").forGetter(NestOccupant::exitAt),
            Codec.INT.fieldOf("slot").forGetter(NestOccupant::slot),
            UUIDUtil.CODEC.fieldOf("id").forGetter(NestOccupant::id)
    ).apply(instance, NestOccupant::new));
}
