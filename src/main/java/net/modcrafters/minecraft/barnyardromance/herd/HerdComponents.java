package net.modcrafters.minecraft.barnyardromance.herd;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.modcrafters.minecraft.barnyardromance.core.Constants;
import net.modcrafters.minecraft.barnyardromance.core.registry.BarnyardRegistrations;

import java.util.UUID;

// What a herd stick and a herd book carry. Kept apart from HerdItems, so the codecs load without making the items.
public final class HerdComponents {
    public static final DataComponentType<HerdTool> HERD_TOOL = DataComponentType.<HerdTool>builder()
            .persistent(HerdToolCodecs.CODEC)
            .networkSynchronized(HerdToolCodecs.STREAM_CODEC)
            .build();
    // The herd book's ID: its list of animals is kept on the server under it (see HerdBookData).
    public static final DataComponentType<UUID> HERD_BOOK = DataComponentType.<UUID>builder()
            .persistent(UUIDUtil.CODEC)
            .networkSynchronized(UUIDUtil.STREAM_CODEC)
            .build();

    private HerdComponents() {
    }

    static void register(BarnyardRegistrations.Sink sink) {
        sink.register(Registries.DATA_COMPONENT_TYPE, id("herd_tool"), () -> HERD_TOOL);
        sink.register(Registries.DATA_COMPONENT_TYPE, id("herd_book"), () -> HERD_BOOK);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
