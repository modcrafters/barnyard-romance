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

import java.util.Optional;

// Which fluids a pail takes (the barnyard_romance:pail_fluids tag), their buckets, and their names and colours. The
// tag is read from the fluid registry's holders on every call, never cached, so a datapack reload changes it without
// a restart.
public final class PailFluids {
    public static final TagKey<Fluid> PAIL_FLUIDS = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pail_fluids"));
    // Every fluid that isn't one of the mod's milks, NeoForge's cow milk among them.
    private static final int WHITE = 0xFFFFFF;

    private PailFluids() {
    }

    public static boolean accepts(Fluid fluid) {
        return fluid != Fluids.EMPTY && BuiltInRegistries.FLUID.wrapAsHolder(fluid).is(PAIL_FLUIDS);
    }

    // The pail fluid this bucket holds. Only a source counts, so a flowing fluid in the tag never stands in for its
    // source, which shares its bucket.
    public static Optional<Fluid> forBucket(Item bucket) {
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(PAIL_FLUIDS)) {
            Fluid fluid = holder.value();
            if (fluid.isSource(fluid.defaultFluidState()) && bucketFor(fluid).filter(item -> item == bucket).isPresent()) {
                return Optional.of(fluid);
            }
        }
        return Optional.empty();
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
        return fluid instanceof MilkFluid milk ? milk.colour() : WHITE;
    }

    // The fluid with this id, if it is still registered.
    static Optional<Fluid> registered(Identifier id) {
        return BuiltInRegistries.FLUID.getOptional(id).filter(fluid -> fluid != Fluids.EMPTY);
    }
}
