package net.modcrafters.minecraft.barnyardromance.item;

import net.modcrafters.minecraft.barnyardromance.command.HerdTool;
import net.minecraft.core.component.DataComponentType;

public final class BarnyardComponents {
    public static final DataComponentType<HerdTool> HERD_TOOL = DataComponentType.<HerdTool>builder()
            .persistent(HerdToolCodecs.CODEC)
            .networkSynchronized(HerdToolCodecs.STREAM_CODEC)
            .build();

    private BarnyardComponents() {
    }
}
