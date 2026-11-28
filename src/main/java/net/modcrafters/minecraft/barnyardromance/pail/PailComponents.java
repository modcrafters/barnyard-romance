package net.modcrafters.minecraft.barnyardromance.pail;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;

// What a pail carries. Kept apart from PailItems, so the codecs load without making the items.
public final class PailComponents {
    // The one fluid a pail holds, and how many millilitres of it.
    //? if >=1.20.5 {
    public static final DataComponentType<PailContents> CONTENTS = DataComponentType.<PailContents>builder()
            .persistent(PailContents.CODEC)
            .networkSynchronized(PailContents.STREAM_CODEC)
            .build();
    //?} else {
    /*public static final DataComponentType<PailContents> CONTENTS = new DataComponentType<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pail_contents"), PailContents.CODEC, PailContents.STREAM_CODEC);
    *///?}

    private PailComponents() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        //? if >=1.20.5 {
        sink.register(Registries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pail_contents"), () -> CONTENTS);
        //?}
    }
}
