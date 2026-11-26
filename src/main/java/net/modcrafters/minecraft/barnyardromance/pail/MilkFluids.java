package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.platform.Services;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

// Milk as a fluid. Goat and sheep milk are the mod's own on both loaders. Cow milk is the loader's own where it has
// one: NeoForge's minecraft:milk, which the mod switches on. Fabric has none, so the mod registers
// barnyard_romance:milk there. The fluids are made when this class loads, which is while the registries are open (see
// register). They are holders because NeoForge's milk exists only once fluids have registered.
public final class MilkFluids {
    public static final int COW_COLOUR = 0xFFFFFF;
    public static final int GOAT_COLOUR = 0xFFF8E7;
    public static final int SHEEP_COLOUR = 0xFDF5E2;
    // The one still and flowing texture every milk shares, tinted with its colour.
    public static final Identifier STILL_TEXTURE = id("block/milk_still");
    public static final Identifier FLOW_TEXTURE = id("block/milk_flow");

    private static final Optional<Holder<Fluid>> LOADER_COW = Services.PLATFORM.loaderMilk();
    public static final Identifier COW_ID = LOADER_COW.map(cow -> cow.unwrapKey().orElseThrow().identifier()).orElse(id("milk"));
    public static final Identifier GOAT_ID = id("goat_milk");
    public static final Identifier SHEEP_ID = id("sheep_milk");

    private static final @Nullable Fluid OWN_COW = LOADER_COW.isPresent()
            ? null
            : Services.PLATFORM.milkFluid(COW_ID.getPath(), () -> Items.MILK_BUCKET, COW_COLOUR);
    private static final Fluid OWN_GOAT = Services.PLATFORM.milkFluid(GOAT_ID.getPath(), () -> bucket("goat_milk_bucket"), GOAT_COLOUR);
    private static final Fluid OWN_SHEEP = Services.PLATFORM.milkFluid(SHEEP_ID.getPath(), () -> bucket("sheep_milk_bucket"), SHEEP_COLOUR);

    public static final Holder<Fluid> COW = OWN_COW == null ? LOADER_COW.orElseThrow() : OWN_COW.builtInRegistryHolder();
    public static final Holder<Fluid> GOAT = OWN_GOAT.builtInRegistryHolder();
    public static final Holder<Fluid> SHEEP = OWN_SHEEP.builtInRegistryHolder();

    private MilkFluids() {
    }

    // The milks this mod registers, which each loader's client gives the milk texture.
    public static List<Fluid> own() {
        return OWN_COW == null ? List.of(OWN_GOAT, OWN_SHEEP) : List.of(OWN_COW, OWN_GOAT, OWN_SHEEP);
    }

    static void register(BarnyardRegistrations.Sink sink) {
        if (OWN_COW != null) {
            sink.register(Registries.FLUID, COW_ID, () -> OWN_COW);
        }
        sink.register(Registries.FLUID, GOAT_ID, () -> OWN_GOAT);
        sink.register(Registries.FLUID, SHEEP_ID, () -> OWN_SHEEP);
    }

    // The milk feature's buckets, by id: a feature reaches another's items only through the registry.
    private static Item bucket(String path) {
        Identifier id = id(path);
        return BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> new IllegalStateException("No item " + id + " is registered"));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
