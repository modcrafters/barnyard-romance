package net.modcrafters.minecraft.barnyardromance.eggs;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;

// What a fertilized egg carries. Kept apart from EggsItems, so the codecs load without making the item.
public final class EggsComponents {
    // A fertilized egg's parents and hatching countdown.
    //? if >=1.20.5 {
    public static final DataComponentType<EggData> EGG_DATA = DataComponentType.<EggData>builder()
            .persistent(EggData.CODEC)
            .networkSynchronized(EggData.STREAM_CODEC)
            .build();
    //?} else {
    /*public static final DataComponentType<EggData> EGG_DATA = new DataComponentType<>(id("egg_data"), EggData.CODEC, EggData.STREAM_CODEC);
    *///?}
    // A fertilized egg that has been out of a nesting box: its species, and its countdown while back in one.
    //? if >=1.20.5 {
    public static final DataComponentType<WildEgg> WILD_EGG = DataComponentType.<WildEgg>builder()
            .persistent(WildEgg.CODEC)
            .networkSynchronized(WildEgg.STREAM_CODEC)
            .build();
    //?} else {
    /*public static final DataComponentType<WildEgg> WILD_EGG = new DataComponentType<>(id("wild_egg"), WildEgg.CODEC, WildEgg.STREAM_CODEC);
    *///?}

    private EggsComponents() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        //? if >=1.20.5 {
        sink.register(Registries.DATA_COMPONENT_TYPE, id("egg_data"), () -> EGG_DATA);
        sink.register(Registries.DATA_COMPONENT_TYPE, id("wild_egg"), () -> WILD_EGG);
        //?}
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
