package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

// Which fluids a pail takes (the barnyard_romance:pail_fluids tag), their buckets, and their names and colours. The
// tag is read from the fluid registry's holders on every call, never cached, so a datapack reload changes it without
// a restart.
public final class PailFluids {
    public static final TagKey<Fluid> PAIL_FLUIDS = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pail_fluids"));
    // The colour of a fluid that isn't one of the mod's milks and isn't the loader's cow milk.
    private static final int WHITE = 0xFFFFFF;

    private PailFluids() {
    }

    public static boolean accepts(Fluid fluid) {
        return fluid != Fluids.EMPTY && BuiltInRegistries.FLUID.wrapAsHolder(fluid).is(PAIL_FLUIDS);
    }

    // The pail fluid this bucket holds. Only a source counts, so a flowing fluid in the tag never stands in for its
    // source, which shares its bucket.
    public static Optional<Fluid> forBucket(Item bucket) {
        return forBucket(bucket, null);
    }

    // The same, but when several fluids in the tag share this bucket, the one the pail already holds comes first, so
    // a pail of one never turns a bucket into another's fluid.
    public static Optional<Fluid> forBucket(Item bucket, @Nullable Identifier preferred) {
        List<Fluid> matches = new ArrayList<>();
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(PAIL_FLUIDS)) {
            Fluid fluid = holder.value();
            if (fluid.isSource(fluid.defaultFluidState()) && bucketFor(fluid).filter(item -> item == bucket).isPresent()) {
                matches.add(fluid);
            }
        }
        return choose(matches, fluid -> preferred != null && preferred.equals(BuiltInRegistries.FLUID.getKey(fluid)));
    }

    // The preferred match if there is one, else the first.
    static <T> Optional<T> choose(List<T> matches, Predicate<T> isPreferred) {
        return matches.stream().filter(isPreferred).findFirst().or(() -> matches.stream().findFirst());
    }

    // The full bucket of this fluid. Empty when it has none: no bucket at all, or an empty bucket.
    public static Optional<Item> bucketFor(Fluid fluid) {
        Item bucket = fluid.getBucket();
        return bucket == Items.AIR || bucket == Items.BUCKET ? Optional.empty() : Optional.of(bucket);
    }

    // The fluid's name from its loader. A fluid that isn't registered any more shows its id.
    public static Component name(Identifier id) {
        return registered(id).map(Services.PLATFORM::fluidName).orElseGet(() -> Component.literal(id.toString()));
    }

    // RGB, for the pail's bar.
    public static int colour(Identifier id) {
        return registered(id).map(PailFluids::colour).orElse(WHITE);
    }

    public static int colour(Fluid fluid) {
        if (fluid instanceof MilkFluid milk) {
            return milk.colour();
        }
        return fluid == MilkFluids.COW.value() ? MilkFluids.COW_COLOUR : WHITE;
    }

    // The fluid with this id, if it is still registered.
    static Optional<Fluid> registered(Identifier id) {
        return BuiltInRegistries.FLUID.getOptional(id).filter(fluid -> fluid != Fluids.EMPTY);
    }
}
