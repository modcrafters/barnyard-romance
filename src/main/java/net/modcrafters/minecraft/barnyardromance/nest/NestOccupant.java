package net.modcrafters.minecraft.barnyardromance.nest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// A hen sitting in a nesting box: her whole saved self (like a bee in a hive), the eggs she'll leave, the game time
// she comes out and the nest she has. Boxes saved when hens laid one egg at a time hold a single "egg".
public record NestOccupant(CompoundTag hen, List<ItemStack> eggs, long exitAt, int slot, UUID id) {
    public static final Codec<NestOccupant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompoundTag.CODEC.fieldOf("hen").forGetter(NestOccupant::hen),
            ItemStack.CODEC.listOf().optionalFieldOf("eggs", List.of()).forGetter(NestOccupant::eggs),
            ItemStack.CODEC.optionalFieldOf("egg").forGetter(occupant -> Optional.empty()),
            Codec.LONG.fieldOf("exitAt").forGetter(NestOccupant::exitAt),
            Codec.INT.fieldOf("slot").forGetter(NestOccupant::slot),
            UUIDUtil.CODEC.fieldOf("id").forGetter(NestOccupant::id)
    ).apply(instance, (hen, eggs, egg, exitAt, slot, id) ->
            new NestOccupant(hen, eggs.isEmpty() ? egg.map(List::of).orElse(List.of()) : eggs, exitAt, slot, id)));

    public NestOccupant {
        eggs = List.copyOf(eggs);
    }
}
