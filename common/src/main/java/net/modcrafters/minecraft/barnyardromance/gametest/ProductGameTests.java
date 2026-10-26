package net.modcrafters.minecraft.barnyardromance.gametest;

import net.modcrafters.minecraft.barnyardromance.milk.Lactation;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraitCodecs;
import net.modcrafters.minecraft.barnyardromance.sex.AnimalTraits;
import net.modcrafters.minecraft.barnyardromance.sex.Sex;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.List;

// In-game checks for milk, sized drops, shearing and taming carnivores, run by each loader's game test server.
public final class ProductGameTests {
    public static final List<BreedingGameTests.Case> CASES = List.of(
            new BreedingGameTests.Case("old_traits_load_without_lactation", 20, ProductGameTests::oldTraitsLoadWithoutLactation)
    );

    private ProductGameTests() {
    }

    // Traits saved before Stage 5 have no lactation and load with none.
    public static void oldTraitsLoadWithoutLactation(GameTestHelper helper) {
        AnimalTraits saved = new AnimalTraits(Sex.FEMALE, 1.0, 0.5, 10L);
        CompoundTag tag = (CompoundTag) AnimalTraitCodecs.CODEC.encodeStart(NbtOps.INSTANCE, saved).getOrThrow();
        tag.remove("lactation");

        AnimalTraits loaded = AnimalTraitCodecs.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
        helper.assertTrue(loaded.lactation().equals(Lactation.NONE), "old traits loaded with " + loaded.lactation());

        Lactation lactation = new Lactation(100L, 200L, 50L, 1, true);
        AnimalTraits milking = saved.withLactation(lactation);
        CompoundTag milkingTag = (CompoundTag) AnimalTraitCodecs.CODEC.encodeStart(NbtOps.INSTANCE, milking).getOrThrow();
        AnimalTraits reloaded = AnimalTraitCodecs.CODEC.parse(NbtOps.INSTANCE, milkingTag).getOrThrow();
        helper.assertTrue(reloaded.lactation().equals(lactation), "lactation came back as " + reloaded.lactation());
        ByteBuf buffer = Unpooled.buffer();
        AnimalTraitCodecs.STREAM_CODEC.encode(buffer, milking);
        AnimalTraits synced = AnimalTraitCodecs.STREAM_CODEC.decode(buffer);
        helper.assertTrue(synced.lactation().equals(lactation), "lactation synced as " + synced.lactation());
        helper.succeed();
    }
}
