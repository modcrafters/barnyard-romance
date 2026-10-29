package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.command.HerdTool;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;

import java.util.UUID;

public final class BarnyardComponents {
    public static final DataComponentType<HerdTool> HERD_TOOL = DataComponentType.<HerdTool>builder()
            .persistent(HerdToolCodecs.CODEC)
            .networkSynchronized(HerdToolCodecs.STREAM_CODEC)
            .build();
    // The herd book's ID: its list of animals is kept on the server under it (see HerdBookData).
    public static final DataComponentType<UUID> HERD_BOOK = DataComponentType.<UUID>builder()
            .persistent(UUIDUtil.CODEC)
            .networkSynchronized(UUIDUtil.STREAM_CODEC)
            .build();

    private BarnyardComponents() {
    }
}
